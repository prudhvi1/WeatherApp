import Combine
import CoreLocation
import Foundation
import Shared

@MainActor
final class WeatherViewModel: NSObject, ObservableObject {
    @Published private(set) var isLoading = false
    @Published private(set) var weather: WeatherSnapshot?
    @Published private(set) var errorMessage: String?

    private let getWeatherForCity: GetWeatherForCity
    private let getWeatherForCurrentLocation: GetWeatherForCurrentLocation
    private let getClothingRecommendation = GetClothingRecommendation()
    private let locationProvider: IOSLocationProvider

    init(apiKey: String = WeatherConfiguration.apiKey) {
        let repository = KtorWeatherRepository.companion.create(apiKey: apiKey)
        let provider = IOSLocationProvider()
        self.locationProvider = provider
        self.getWeatherForCity = GetWeatherForCity(repository: repository)
        self.getWeatherForCurrentLocation = GetWeatherForCurrentLocation(
            repository: repository,
            locationProvider: provider
        )
        super.init()
    }

    func search(city: String) {
        let city = city.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !city.isEmpty else {
            errorMessage = "Enter a city name"
            return
        }

        beginLoading()
        getWeatherForCity.invoke(city: city) { [weak self] result, error in
            Task { @MainActor in
                self?.finish(result: result, error: error)
            }
        }
    }

    func fetchCurrentLocation() {
        beginLoading()
        locationProvider.requestPermissionIfNeeded()
        getWeatherForCurrentLocation.invoke { [weak self] result, error in
            Task { @MainActor in
                self?.finish(result: result, error: error)
            }
        }
    }

    var clothingRecommendation: String? {
        guard let weather else { return nil }
        return getClothingRecommendation.invoke(snapshot: weather)
    }

    private func beginLoading() {
        isLoading = true
        errorMessage = nil
    }

    private func finish(result: WeatherResult?, error: Error?) {
        isLoading = false

        if let error {
            errorMessage = error.localizedDescription
            return
        }

        guard let result else {
            errorMessage = "Something went wrong. Please try again"
            return
        }

        if let success = result as? WeatherResultSuccess {
            weather = success.snapshot
            errorMessage = nil
        } else if let failure = result as? WeatherResultFailure {
            errorMessage = userMessage(for: failure.message)
        } else {
            errorMessage = "Something went wrong. Please try again"
        }
    }

    private func userMessage(for message: String) -> String {
        let lowercased = message.lowercased()
        if lowercased.contains("cannot be empty") {
            return "Enter a city name"
        }
        if lowercased.contains("could not find location") || lowercased.contains("no weather location found") {
            return "We could not find that city"
        }
        if message.contains("401") {
            return "The OpenWeather API key is invalid or not active yet"
        }
        if message.contains("429") {
            return "The weather service request limit was reached"
        }
        if lowercased.contains("failed to fetch") {
            return "Check your internet connection and try again"
        }
        return message.isEmpty ? "Something went wrong. Please try again" : message
    }
}

private enum WeatherConfiguration {
    private static let apiKeyName = "OPENWEATHER_API_KEY"

    static var apiKey: String {
        let environment = ProcessInfo.processInfo.environment[apiKeyName]?
            .trimmingCharacters(in: .whitespacesAndNewlines)
        if let environment, !environment.isEmpty {
            return environment
        }

        let configured = Bundle.main.object(forInfoDictionaryKey: apiKeyName) as? String
        return configured?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    }
}

private final class IOSLocationProvider: NSObject, CLLocationManagerDelegate, CurrentLocationProvider {
    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    func requestPermissionIfNeeded() {
        guard CLLocationManager.locationServicesEnabled() else { return }
        if manager.authorizationStatus == .notDetermined {
            manager.requestWhenInUseAuthorization()
        } else if manager.authorizationStatus == .authorizedAlways ||
                    manager.authorizationStatus == .authorizedWhenInUse {
            manager.requestLocation()
        }
    }

    func currentLocation(completionHandler: @escaping (Coordinates?, Error?) -> Void) {
        guard CLLocationManager.locationServicesEnabled() else {
            completionHandler(nil, LocationError.servicesDisabled)
            return
        }

        switch manager.authorizationStatus {
        case .authorizedAlways, .authorizedWhenInUse:
            pendingCompletion = completionHandler
            manager.requestLocation()
        case .denied, .restricted:
            completionHandler(nil, LocationError.permissionDenied)
        case .notDetermined:
            pendingCompletion = completionHandler
            manager.requestWhenInUseAuthorization()
        @unknown default:
            completionHandler(nil, LocationError.unavailable)
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard let pendingCompletion else { return }
        switch manager.authorizationStatus {
        case .authorizedAlways, .authorizedWhenInUse:
            manager.requestLocation()
        case .denied, .restricted:
            self.pendingCompletion = nil
            pendingCompletion(nil, LocationError.permissionDenied)
        default:
            break
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let completion = pendingCompletion
        pendingCompletion = nil
        guard let location = locations.last else {
            completion?(nil, LocationError.unavailable)
            return
        }
        completion?(
            Coordinates(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude),
            nil
        )
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        let completion = pendingCompletion
        pendingCompletion = nil
        completion?(nil, error)
    }

    private var pendingCompletion: ((Coordinates?, Error?) -> Void)?

    private enum LocationError: LocalizedError {
        case permissionDenied
        case servicesDisabled
        case unavailable

        var errorDescription: String? {
            switch self {
            case .permissionDenied:
                return "Location permission is required to fetch current weather"
            case .servicesDisabled, .unavailable:
                return "Turn on location services and try again"
            }
        }
    }
}
