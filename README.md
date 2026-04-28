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

 
Author
Simphiwe, Mbuso, Nhlamulo - Software Development Students
