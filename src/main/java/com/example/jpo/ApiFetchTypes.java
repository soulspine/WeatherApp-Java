package com.example.jpo;

public enum ApiFetchTypes {
    TEMPERATURE_2M             (1L,  "temperature_2m",             "°C",   "Temperatura na wysokości 2m"),
    PRESSURE_MSL              (1L << 1,  "pressure_msl",               "hPa",  "Ciśnienie na poziomie morza"),
    RELATIVE_HUMIDITY_2M     (1L << 2,  "relative_humidity_2m",       "%",    "Wilgotność względna na wysokości 2m"),
    DEW_POINT_2M             (1L << 3,  "dew_point_2m",               "°C",   "Temperatura punktu rosy na wysokości 2m"),
    APPARENT_TEMPERATURE     (1L << 4,  "apparent_temperature",       "°C",   "Temperatura odczuwalna"),
    PRECIPITATION_PROBABILITY(1L << 5,  "precipitation_probability",  "%",    "Prawdopodobieństwo opadów"),
    PRECIPITATION            (1L << 6,  "precipitation",              "mm",   "Suma opadów"),
    RAIN                     (1L << 7,  "rain",                       "mm",   "Opady deszczu"),
    SHOWERS                  (1L << 8,  "showers",                   "mm",   "Przelotne opady"),
    SNOWFALL                 (1L << 9,  "snowfall",                   "cm",   "Opad śniegu"),
    SNOW_DEPTH               (1L << 10, "snow_depth",                 "cm",   "Głębokość pokrywy śnieżnej"),
    SOIL_TEMPERATURE_0CM     (1L << 11, "soil_temperature_0cm",       "°C",   "Temperatura gleby na 0 cm"),
    SOIL_TEMPERATURE_6CM     (1L << 12, "soil_temperature_6cm",       "°C",   "Temperatura gleby na 6 cm"),
    SOIL_TEMPERATURE_18CM    (1L << 13, "soil_temperature_18cm",      "°C",   "Temperatura gleby na 18 cm"),
    SOIL_TEMPERATURE_54CM    (1L << 14, "soil_temperature_54cm",      "°C",   "Temperatura gleby na 54 cm"),
    SOIL_MOISTURE_0_TO_1CM   (1L << 15, "soil_moisture_0_to_1cm",    "m³/m³","Wilgotność gleby 0-1 cm"),
    SOIL_MOISTURE_1_TO_3CM   (1L << 16, "soil_moisture_1_to_3cm",    "m³/m³","Wilgotność gleby 1-3 cm"),
    SOIL_MOISTURE_3_TO_9CM   (1L << 17, "soil_moisture_3_to_9cm",    "m³/m³","Wilgotność gleby 3-9 cm"),
    SOIL_MOISTURE_9_TO_27CM  (1L << 18, "soil_moisture_9_to_27cm",   "m³/m³","Wilgotność gleby 9-27 cm"),
    SOIL_MOISTURE_27_TO_81CM (1L << 19, "soil_moisture_27_to_81cm",  "m³/m³","Wilgotność gleby 27-81 cm"),
    ET0_FAO_EVAPOTRANSPIRATION (1L << 20, "et0_fao_evapotranspiration", "mm", "Potencjalna ewapotranspiracja FAO"),
    EVAPOTRANSPIRATION       (1L << 21, "evapotranspiration",         "mm",   "Ewapotranspiracja"),
    VAPOUR_PRESSURE_DEFICIT  (1L << 22, "vapour_pressure_deficit",    "hPa",  "Deficyt ciśnienia pary wodnej"),
    VISIBILITY               (1L << 23, "visibility",                 "m",    "Widzialność"),
    CLOUD_COVER_HIGH         (1L << 24, "cloud_cover_high",           "%",    "Zachmurzenie wysokie"),
    CLOUD_COVER_MID          (1L << 25, "cloud_cover_mid",            "%",    "Zachmurzenie średnie"),
    CLOUD_COVER_LOW          (1L << 26, "cloud_cover_low",            "%",    "Zachmurzenie niskie"),
    CLOUD_COVER              (1L << 27, "cloud_cover",                "%",    "Całkowite zachmurzenie"),
    SURFACE_PRESSURE         (1L << 28, "surface_pressure",           "hPa",  "Ciśnienie na powierzchni"),
    WEATHER_CODE             (1L << 29, "weather_code",               "",     "Kod pogody"),
    WIND_SPEED_10M           (1L << 30, "wind_speed_10m",             "km/h", "Prędkość wiatru na 10 m"),
    WIND_SPEED_80M           (1L << 31, "wind_speed_80m",             "km/h", "Prędkość wiatru na 80 m"),
    WIND_SPEED_120M          (1L << 32, "wind_speed_120m",            "km/h", "Prędkość wiatru na 120 m"),
    WIND_SPEED_180M          (1L << 33, "wind_speed_180m",            "km/h", "Prędkość wiatru na 180 m"),
    WIND_DIRECTION_10M       (1L << 34, "wind_direction_10m",         "°",    "Kierunek wiatru na 10 m"),
    WIND_DIRECTION_80M       (1L << 35, "wind_direction_80m",         "°",    "Kierunek wiatru na 80 m"),
    WIND_DIRECTION_120M      (1L << 36, "wind_direction_120m",        "°",    "Kierunek wiatru na 120 m"),
    WIND_DIRECTION_180M      (1L << 37, "wind_direction_180m",        "°",    "Kierunek wiatru na 180 m"),
    WIND_GUSTS_10M           (1L << 38, "wind_gusts_10m",             "km/h", "Porywy wiatru na 10 m"),
    TEMPERATURE_80M          (1L << 39, "temperature_80m",            "°C",   "Temperatura na 80 m"),
    TEMPERATURE_120M         (1L << 40, "temperature_120m",           "°C",   "Temperatura na 120 m"),
    TEMPERATURE_180M         (1L << 41, "temperature_180m",           "°C",   "Temperatura na 180 m");

    public final long bit;
    public final String description;
    public final String unit;
    public final String polishDescription;

    ApiFetchTypes(long bit, String description, String unit, String polishDescription) {
        this.bit = bit;
        this.description = description;
        this.unit = unit;
        this.polishDescription = polishDescription;
    }
}