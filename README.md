# 📋 Attendance App

Welcome to the **Attendance App** repository! This mobile app, built with **Kotlin** and **XML**, integrates **Firebase Firestore** to manage attendance efficiently. Users can log in, mark attendance with a photo, and view their attendance history in a sleek and easy-to-navigate interface. 

---

## 🚀 Features

- 🔑 **Login and Signup**: Secure Firebase Authentication for user account management.
- 📸 **Attendance Photo**: Check-in/check-out feature with camera capture for attendance proof.
- 📄 **Diary Upload**: Users can upload diary entries with text and photos, viewable in a dynamic list (RecyclerView).
- 🗂 **Attendance History**: Track past check-in/out data.
- ⚙️ **Profile Management**: Users can update personal details such as name and student ID.
- 🎨 **Beautiful UI**: Designed with a focus on simplicity and user-friendliness.

---

## 🛠️ Tech Stack

- **Kotlin**: Backend logic and functionality.
- **XML**: Frontend layout design.
- **Firebase Firestore**: Cloud database for attendance data, diary entries, and user profiles.
- **Firebase Authentication**: Secure login and signup functionality.

---

## 📲 Getting Started

Follow these steps to set up and run the project on your local machine:

1. **Clone the repository**:

   ```bash
   git clone https://github.com/your-username/attendance-app.git

2. Open the project in Android Studio.

3. Configure Firebase:

- Create a new project in Firebase Console.
- Add your Android app to Firebase and download the google-services.json file.
- Place google-services.json in your app's /app directory.
- Enable Firestore Database and Firebase Authentication.
- Build and Run the app on your Android device/emulator.

🔐 Firebase Setup
Make sure to set up the following Firebase services for a fully functional experience:

- Firebase Firestore: Stores attendance records, diary entries, and user data.
- Firebase Authentication: Manages user sign-in/sign-up securely.
