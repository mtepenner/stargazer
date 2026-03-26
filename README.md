# Stargazer 🌟

## About
Stargazer is an Augmented Reality (AR) Android application designed to help users identify celestial bodies. By leveraging device sensors and a live camera feed, the app calculates the user's heading and tilt to project stars and markers directly onto the screen, creating an immersive stargazing experience.

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Features 🚀
* **Live AR Camera Feed:** Displays a real-time background feed using Android's CameraX library.
* **Real-Time Sensor Tracking:** Calculates Azimuth (compass heading) and Pitch (device tilt) using the hardware Rotation Vector sensor.
* **AR Overlay:** Renders celestial bodies and a targeting crosshair directly over the camera feed using Jetpack Compose Canvas.
* **Debugging HUD:** A built-in Heads Up Display (HUD) showing raw heading and tilt values for testing and calibration.
* **Mock Star Data:** Includes a foundational coordinate system with test markers (North, South, East, West, Zenith) to demonstrate AR projection math.

## Technologies Used 🛠️
* **Kotlin:** Core programming language.
* **Jetpack Compose:** Modern toolkit for building the native UI and rendering the AR canvas overlay.
* **CameraX:** Handles the live camera preview lifecycle and surface rendering.
* **Android Hardware Sensors:** Utilizes the accelerometer and compass via the `Sensor.TYPE_ROTATION_VECTOR` for smooth 3D spatial orientation.
* **Google Play Services Location:** Integrated for future support of calculating accurate, location-based star positions.

## Installation 💻
1. Clone the repository to your local machine:
   ```bash
   git clone [https://github.com/yourusername/stargazer.git](https://github.com/yourusername/stargazer.git)
   ```
2. Open the project in **Android Studio**.
3. Sync the Gradle files to download the required dependencies (such as Compose and CameraX).
4. Connect a physical Android device (minimum SDK 26) via USB or Wireless Debugging. *Note: An emulator may not properly simulate the camera and rotation vector sensors required for the AR experience.*
5. Build and run the project.

## Usage 📱
1. **Permissions:** Upon launching the app for the first time, grant the requested Camera and Location permissions.
2. **Explore the Sky:** Hold your phone up and move it around. The app will display a live camera feed with an AR overlay.
3. **Identify Markers:** You will see mock celestial bodies (e.g., "North Star", "Zenith") anchored to their correct compass directions and altitudes in the sky.

## Contributing 🤝
Contributions are welcome! If you'd like to help improve Stargazer, please follow these steps:
1. Fork the repository.
2. Create a new feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## License 📜
Distributed under the MIT License. See `LICENSE` for more information.
