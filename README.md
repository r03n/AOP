# Academic Organization Platform (AOP)

The Academic Organization Platform (AOP) is a robust Java-based desktop application designed to streamline the creation, registration, and management of school events. Built with a focus on clean architecture, the platform provides a comprehensive ecosystem for students and faculty to organize academic gatherings.

## Role-Based Access
The application features a strict, three-tier user hierarchy utilizing Object-Oriented Programming (OOP) inheritance:

* **Admin:** Has unrestricted global access to manage all events in the system, purge accounts, and freely promote or demote user roles.
* **Organizer:** Capable of creating custom events, managing participant rosters, removing attendees, and sending direct event invitations.
* **Participant:** Designed for standard users to browse the global event board, manage personal registrations, and view event invitations.

## Key Features
* **Strict Capacity Enforcement:** The database automatically prevents standard registrations and administrative forced additions if an event has reached its maximum participant capacity.
* **Comprehensive Event Details:** Events are tracked using custom titles, comprehensive descriptions, capacities, and dedicated date and time parameters.
* **Profile Management:** Users can personalize their accounts by updating their Full Name, Age, Department, and Year Level.

## Technology Stack and Architecture
* **Frontend:** Java Swing, utilizing a modular View-Controller architecture.
* **Backend and Database:** SQLite via JDBC.
* **Design Patterns:** Heavily incorporates core OOP principles including Encapsulation, Polymorphism, Abstraction, and Exception Handling to ensure a highly scalable and crash-resistant application.

## Default Admin Account:
```yml
Username: admin
Password: admin123
```

## To-Do
- [x] Fix Directory Tree
- [x] Add TIME to Events
- [x] Show Organizer's Name in Events (Instead of username)
- [x] Show Participant's Name in Events (Instead of username)
- [x] Fix Detailed Event View
- [x] Move Description from Table View to Detailed Event View
- [x] Show Invited Participants in Detailed Event View
- [x] Show Detailed Event View when Double Clicked by Participant
- [x] Show full details of Participant to ADMIN
- [x] Disallow Participant from Registering to Events if PROFILE is NOT filled out
- [x] Allow Participant to BACKOUT / UNREGISTER from Events
- [x] Add CHANGE PASSWORD Option to all Users
- [x] Recheck ALL EXCEPTIONS
- [x] Improve Create/Edit Event View
- [x] Improve Detailed Event View
