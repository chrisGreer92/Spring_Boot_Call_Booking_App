# Call Booking System

Self-hosted call booking system built with Java + Spring Boot and PostgreSQL, containerised with Docker and deployed on fly.io.

Designed to let users schedule calls via a simple web interface while providing an authenticated admin backend for managing bookings. 

The system follows a clean architecture approach with a separation of DTOs, entities, and controllers, and uses Spring Data JPA for database integration, Flyway for versioned migrations, and Spring Security for admin-only access. 

The RESTful API supports creating, listing, updating, and cancelling bookings, with built-in request validation and structured error handling. 

A lightweight frontend (HTML + JavaScript with FullCalendar) integrates directly with the backend API, enabling users to select available time slots and submit requests (see https://github.com/chrisGreer92/website). 
