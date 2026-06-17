Due to Youtube Removing our video Please download here/ here is a new link 

https://youtu.be/Wzf-lrJqzkM?si=BbeZvarFn3uzQMZo

Channel Name to watch the video
www.youtube.com/@simi_samu

BankSmart 💰
Budgeting made fun, one quest at a time.

Table of Contents
Project Overview
Who It's For
Key Features
Our Custom Features
Tech Stack
Design Decisions
GitHub & GitHub Actions
Getting Started
Methodology
Screenshots
Video Demo
Team
-----------------------------------------------------------------------------------------------------------------------------

BankSmart 
Budgeting made fun, one quest at a time.
Project Overview
BankSmark is an Android-based personal finance manager designed to solve "budgeting burnout." By blending robust financial tracking with engaging gamification mechanics, the app transforms daily expense logging into a rewarding experience.

Built as a final-year project, this application focuses on simplicity, visual clarity, and habit formation.

Key Features
Safe-to-Spend Dashboard: Instantly see your daily allowance based on your monthly goals.

Frictionless Logging: Add transactions in under 3 seconds with our optimized "Quick-Add" interface.

Gamified Progression: Earn XP and level up from "Rookie" to "Elite" as you maintain your financial health.

Daily Streaks: Keep your "Flame" alive by logging expenses consistently.

Achievement Badges: Unlock digital trophies for milestones like "7-Day Saver" or "Budget Master."

Visual Infographics: Beautifully rendered charts to help you visualize spending patterns.

The "Safe-to-Spend" Engine: A real-time dashboard that calculates exactly how much you can spend today without ruining your monthly goals.

XP & Leveling System: Progress from a "Financial Novice" to a "Wealth Master." Every transaction logged and every budget met earns you Experience Points (XP).

Streak Protection: Visual motivation via a "Daily Flame." Don't let your logging streak break!

Achievement Gallery: A dedicated space to view unlocked digital badges, rewarding positive financial behaviors like "Emergency Fund Starter" or "7-Day Discipline."

Fast-Entry Interface: A streamlined, one-tap entry system inspired by the "MyMoney" philosophy to ensure zero friction while on the go.

OUR OWN FEATURE
Gamification Logic
The "Financial Fitness" algorithm is handled by a custom service that triggers every time a transaction is saved. It checks:
Consecutive Days: If currentDate - lastEntryDate == 1, the streak is incremented.
Enhanced Features in BankSmart
Digital Receipt Capture: Users can now take pictures of physical receipts directly within the app. These images are linked to specific transactions, providing a digital paper trail for tax time or warranty tracking.

Dynamic Category Management: Unlike rigid budgeting tools, BankSmart allows users to create, edit, and delete custom categories. You can personalize your budget with unique names and icons to match your specific lifestyle.

Frictionless Quick-Add: A streamlined, one-tap entry system inspired by the "MyMoney" philosophy to ensure zero friction while on the go.

Technical Deep Dive (Updated)
Media Storage & URI Handling
To handle the "Take Picture" feature, BankSmart utilizes the Android CameraX API or Intent-based Image Capture.

Storage Strategy: To keep the app fast and the database light, the app does not store the actual image in the SQLite database. Instead, it saves the image to the Internal App Storage and stores the File Path (URI) in the transactions table.

Permissions: Implements the latest Android Scoped Storage guidelines to ensure user privacy and data security.

Tech Stack
Platform: Android

Language: Kotlin

Database: Room (SQLite) for local data persistence.

UI Components: Material Design 3, Lottie (for gamification animations).

Getting Started
Prerequisites
Android Studio Ladybug (or newer)

Android SDK Level 34+

A physical Android device or Emulator

Installation
Clone the repository:

Installation
Clone the repository:

Bash
https://github.com/Hlamulo-Ndhlovu/BankSmart.git 
Open the project:
Launch Android Studio and select Open -> Browse to the cloned folder.

Build the project:
Allow Gradle to sync and download necessary dependencies.

Run the app:
Select your device/emulator and click the Run button.

Run the app:
Select your device/emulator and click the Run button.



Methodology
This project was developed using Agile Methodology. The development was split into five critical phases:

Research: Competitive analysis of Spendee, Vault22, and MyMoney.

Planning: Defined WBS, RACI matrix, and network diagrams.

Design: High-fidelity UI/UX wireframing.

Build: Iterative development via 2-week Sprints.

Evaluate: User testing and performance optimization.

License
This project is licensed under the MIT License - see the LICENSE file for details.

Demo Video link:
https://youtu.be/j2VHyl9YAmw?si=VQ1xLtgTTKC6V0Yx
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 10 (2)" src="https://github.com/user-attachments/assets/8d3cac67-3a0b-424e-a539-eac5684bbd53" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 06" src="https://github.com/user-attachments/assets/6fdac5fb-0ab4-40d6-ba03-58b793b935ba" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 07" src="https://github.com/user-attachments/assets/63b2473a-8a60-414c-939f-9d8c38b2cf21" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 07 (1)" src="https://github.com/user-attachments/assets/dda5cd6e-9756-4fe8-9c87-daa9df88d194" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 08" src="https://github.com/user-attachments/assets/e58bcb45-88f4-45b9-94d9-5bdf3befb508" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 08 (1)" src="https://github.com/user-attachments/assets/88130a1c-8dc5-47d8-ad57-4890074af01a" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 09" src="https://github.com/user-attachments/assets/ba4c9fc9-e4e9-4a29-91d9-e735e3811512" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 09 (1)" src="https://github.com/user-attachments/assets/71680f8a-cc73-495f-83ae-5f78cdd09504" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 10" src="https://github.com/user-attachments/assets/c167c6b1-8735-405c-85ef-704e11221255" />
<img width="720" height="1612" alt="WhatsApp Image 2026-04-28 at 17 39 10 (1)" src="https://github.com/user-attachments/assets/7b2215ca-5a4c-4bde-86a9-743312442cec" />


 
Author
Simphiwe, Mbuso, Nhlamulo - Software Development Students





Part 3 
# BankSmart 💰

**Budgeting made fun, one quest at a time.**

## Table of Contents
- [Project Overview](#project-overview)
- [Who It's For](#who-its-for)
- [Key Features](#key-features)
- [Our Custom Features](#our-custom-features)
- [Tech Stack](#tech-stack)
- [Design Decisions](#design-decisions)
- [GitHub & GitHub Actions](#github--github-actions)
- [Getting Started](#getting-started)
- [Methodology](#methodology)
- [Screenshots](#screenshots)
- [Video Demo](#video-demo)
- [Team](#team)

---

## Project Overview

BankSmart is an Android-based personal finance manager designed to solve "budgeting burnout." By blending robust financial tracking with engaging gamification mechanics, the app transforms daily expense logging into a rewarding experience.

Built as a final-year project, BankSmart focuses on **simplicity**, **visual clarity**, and **habit formation** — helping users build sustainable financial habits without the tedium typically associated with budgeting apps.

## Who It's For

BankSmart is built for:
- Students and young professionals who find traditional budgeting apps boring or overwhelming
- Anyone trying to build a consistent habit of tracking their daily spending
- Users who respond well to gamified motivation (XP, levels, streaks, badges) rather than spreadsheets and raw numbers
- People who want a fast, low-friction way to log expenses on the go

## Key Features

- **Safe-to-Spend Dashboard** — Instantly see your daily allowance based on your monthly goals
- **Frictionless Quick-Add** — Add transactions in under 3 seconds with our optimized "Quick-Add" interface, inspired by the "MyMoney" philosophy
- **Gamified Progression** — Earn XP and level up from "Financial Novice" to "Wealth Master" / "Rookie" to "Elite" as you maintain your financial health
- **Daily Streaks** — Keep your "Flame" alive by logging expenses consistently; don't let your streak break!
- **Achievement Badges** — Unlock digital trophies for milestones like "7-Day Saver," "Emergency Fund Starter," and "Budget Master"
- **Visual Infographics** — Beautifully rendered charts to help you visualize spending patterns
- **Dynamic Category Management** — Create, edit, and delete custom budget categories with unique names and icons
- **Digital Receipt Capture** — Take pictures of physical receipts directly within the app, linked to specific transactions for tax time or warranty tracking

## Our Custom Features

This project implements **two custom features** beyond the core budgeting functionality:

### 1. Authentication
Secure user login and account management, ensuring each user's financial data is private and persisted to their own account.

### 2. Gamification System (Badges + Leveling)
A custom "Financial Fitness" engine that runs every time a transaction is saved:
- **XP & Leveling** — Every logged transaction and met budget goal earns XP, progressing the user from "Financial Novice" to "Wealth Master"
- **Streak Tracking** — If `currentDate - lastEntryDate == 1`, the daily streak ("Flame") is incremented; breaking the pattern resets it
- **Achievement Badges** — An Achievement Gallery displays unlocked badges, rewarding positive financial behaviors (e.g., "7-Day Discipline," "Emergency Fund Starter")

## Tech Stack

| Component | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| Database | Room (SQLite) for local data persistence and Firebase|
| UI Components | Material Design 3, Lottie (gamification animations) |
| Media | CameraX API / Intent-based Image Capture |

## Design Decisions

- **Room over raw SQLite**: Chosen for type-safe queries, compile-time verification, and easy LiveData/Flow integration with the UI.
- **Image storage strategy**: Receipt images are *not* stored in the database. Instead, images are saved to Internal App Storage and only the file path (URI) is stored in the `transactions` table — keeping the database lightweight and the app fast.
- **Scoped Storage compliance**: Implements the latest Android Scoped Storage guidelines to protect user privacy and meet modern Android security requirements.
- **MVVM architecture**: Separates UI, business logic, and data layers for testability and maintainability.
- **Gamification as a separate service**: The XP/streak/badge logic is encapsulated in its own service layer, triggered on transaction save, keeping gamification logic decoupled from core transaction logic.
- **Material Design 3**: Used for a modern, accessible, and consistent UI/UX across the app.

## GitHub & GitHub Actions

- **Source control**: All Kotlin source code is hosted on GitHub as the primary submission (no zip files).
- **Commit history**: Iterative commits reflect our Agile sprint-based development process.
- **GitHub Actions**: A CI workflow (`.github/workflows/android-ci.yml`) automatically builds and tests the app on every push and pull request to `main`, running:
  - Gradle build (`./gradlew build`)
  - Unit tests (`./gradlew test`)
- This ensures the app compiles successfully and passes tests before changes are merged, catching errors early in the development cycle.

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK Level 34+
- A physical Android device or Emulator

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Hlamulo-Ndhlovu/BankSmart.git
   ```
2. **Open the project**
   Launch Android Studio and select **Open** → browse to the cloned folder.
3. **Build the project**
   Allow Gradle to sync and download necessary dependencies.
4. **Run the app**
   Select your device/emulator and click **Run**.

## Methodology

This project was developed using **Agile Methodology**, split into five critical phases:

1. **Research** — Competitive analysis of Spendee, Vault22, and MyMoney
2. **Planning** — Defined Work Breakdown Structure (WBS), RACI matrix, and network diagrams
3. **Design** — High-fidelity UI/UX wireframing
4. **Build** — Iterative development via 2-week Sprints
5. **Evaluate** — User testing and performance optimization

## Screenshots



## Video Demo

🎥 [Watch the demo video here](#) <!-- Replace # with your video link -->

## Team

Developed by Hlamulo Ndhlovu/Simphiwe Dladla/ Mbuso Sihlali and team as a final-year project.
