import SwiftUI
import Shared

struct ContentView: View {
    @StateObject private var viewModel = WeatherViewModel()
    @State private var city = "Chicago"

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    searchSection

                    if viewModel.isLoading {
                        ProgressView("Loading weather…")
                            .frame(maxWidth: .infinity)
                    }

                    if let errorMessage = viewModel.errorMessage {
                        Text(errorMessage)
                            .foregroundStyle(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .accessibilityLabel("Weather error: " + errorMessage)
                    }

                    if let weather = viewModel.weather {
                        weatherSection(weather)
                    }
                }
                .padding()
            }
            .navigationTitle("Weather")
        }
    }

    private var searchSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("City", text: $city)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.words)
                .submitLabel(.search)
                .onSubmit {
                    viewModel.search(city: city)
                }

            HStack {
                Button("Search") {
                    viewModel.search(city: city)
                }
                .buttonStyle(.borderedProminent)

                Button("Current Location") {
                    viewModel.fetchCurrentLocation()
                }
                .buttonStyle(.bordered)
            }
        }
    }

    @ViewBuilder
    private func weatherSection(_ weather: WeatherSnapshot) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(weather.city)
                .font(.title2.weight(.semibold))

            weatherRow("Temperature", value: "\(weather.temperature.formatted(.number.precision(.fractionLength(0))))°F")
            weatherRow("Feels like", value: "\(weather.feelsLike.formatted(.number.precision(.fractionLength(0))))°F")
            weatherRow("Condition", value: weather.conditions.first?.description_.capitalized ?? "Unknown")
            weatherRow("Humidity", value: "\(weather.humidityPercent)%")

            if let recommendation = viewModel.clothingRecommendation {
                Label(recommendation, systemImage: "tshirt")
                    .padding(.top, 4)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16))
    }

    private func weatherRow(_ label: String, value: String) -> some View {
        HStack {
            Text(label)
                .foregroundStyle(.secondary)
            Spacer()
            Text(value)
                .fontWeight(.medium)
        }
    }
}

#Preview {
    ContentView()
}
