# Smart Banking System with Fraud Detection

A secure, full-stack banking application built with **Java (Backend)** and **Vanilla JavaScript (Frontend)**. This project demonstrates the application of **Data Structures and Algorithms (DSA)** to detect financial fraud in real-time.

![Preview](https://via.placeholder.com/800x400?text=Smart+Banking+Dashboard+Preview) 
*(Note: Replace this image link with your actual screenshot after uploading)*

## 🚀 Key Features

### 1. 🛡️ DSA-Powered Fraud Detection
The core innovation of this project is its usage of algorithms to secure transactions:
*   **Graph Cycle Detection (DFS)**: Automatically detects **Money Laundering Loops** (e.g., User A -> User B -> User C -> User A).
*   **Priority Queue (Max-Heap)**: Ranks suspicious transactions by **Risk Score** so admins can review the most dangerous ones first.
*   **Sliding Window Algorithm**: Identifies **High-Velocity Fraud** (e.g., multiple transactions in a short burst).
*   **Hashing**: Uses SHA-256 for secure password storage.

### 2. 💻 Modern Frontend
*   **Glassmorphism UI**: A premium, translucent design using pure CSS3.
*   **Single Page Application (SPA)**: Fast, seamless navigation between Login, Dashboard, and History views without page reloads.

### 3. ☕ Pure Java Backend
*   **No Frameworks**: Built using the standard Java 11+ library (`com.sun.net.httpserver`) to demonstrate deep understanding of HTTP and core Java.
*   **REST API**: Custom-built endpoints for Authentication and Transactions.

## 🛠️ Tech Stack
*   **Backend**: Java (JDK 11+), no external JARs required.
*   **Frontend**: HTML5, CSS3, Vanilla JavaScript.
*   **Data Structures**: Graphs (Adjacency List), Heaps, HashMaps.

## 📦 How to Run

1.  **Compile the Java Backend**:
    Open a terminal in the project folder and run:
    ```bash
    javac -d bin -sourcepath src src/com/banking/Main.java
    ```

2.  **Start the Server**:
    ```bash
    java -cp bin com.banking.Main
    ```

3.  **Access the App**:
    Open your browser and go to:
    [http://localhost:8081/](http://localhost:8081/)

## 🧪 Testing Fraud Scenarios
*   **Money Laundering**: Create a chain of transfers $A \to B \to C \to A$. The system will flag this as "High Risk".
*   **Rapid Transfer**: Perform >5 transactions in 1 minute to trigger the velocity check.
