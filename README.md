# PickBot — Custom Poll Bot for Telegram

**Repository:** [Serwios/PickBot](https://github.com/Serwios/PickBot)

---

## 📌 Overview

PickBot is a custom Telegram bot designed to facilitate interactive polls within Telegram groups. Unlike standard Telegram polls, PickBot offers enhanced customization and functionality, allowing users to create more engaging and tailored polling experiences.

---

## ⚙️ Features

- **Customizable Poll Options**: Users can define specific options for each poll, providing flexibility beyond Telegram's default settings.
- **User Interaction Tracking**: The bot can track and display user participation, offering insights into engagement levels.
- **Admin Controls**: Administrators have the ability to manage polls, including starting, ending, and deleting polls as needed.
- **Persistent Polling**: Polls remain active until manually closed, ensuring that users can participate at their convenience.
- **Multilingual Support**: The bot supports multiple languages, making it accessible to a global audience.
- **Time-out for polling**: You can set end time when polling will automatically close

---

## 🚀 Installation

To deploy PickBot, follow these steps:

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/Serwios/PickBot.git
   cd PickBot
   ```
2. **Setup envs**:
In .env set TELEGRAM_API_TOKEN=your_telegram_bot_token

3. **Build and run app**
Using Maven:

mvn clean install
mvn exec:java

Alternatively, using Docker:

docker-compose up --build

4. **Access the Bot**:

Start a chat with your bot on Telegram and follow the on-screen instructions to create and manage polls.



