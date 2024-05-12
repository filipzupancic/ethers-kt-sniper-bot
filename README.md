# <h1 align="center"> ethers-kt sniper bot </h1>
This project develops a sniper bot for EVM-compatible blockchains using the Spring Boot framework and Kotlin programming language, leveraging the ethers-kt library to interact with blockchain networks.

## ✨Features:
- **Sniping**: The bot monitors new UniswapV2Pair events on chain and executes trades after appropriate checks.
- **High Performance**: By using the ethers-kt library, the bot is optimized for performance and efficiency.

## 🚀 Quickstart:
1. Clone the repository
2. Create a new file `.env` in the root directory and add the properties that are required for the bot to run. Example can be found in `.env.example`
3. The bot can be run from terminal using ./gradlew bootRun or just run the main function in EthersKtSniperBotApplication.kt

## 📦 Project Structure:
- `src/main/kotlin/EthersKtSniperBotApplication.kt`: Main entry point of the application
- `src/main/kotlin/strategies/EthersKtSniperBotStrategy.kt`: Sniper bot strategy implementation
- `src/main/abi/` : ABI files for the smart contracts used in the project. Kotlin wrappers are generated from these files.
- `build.gradle.kts`: Gradle build file for the project where dependencies (like ethers-kt) are defined

## ⚠️ Disclaimer:

This project and the strategies provided are for educational purposes only. The examples demonstrate how to use EthersKt for interacting with Ethereum smart contracts but do not include complete trading logic. Implementation of actual trading logic is left to the discretion and responsibility of the reader.

The users of this project should understand that interacting with smart contracts and decentralized finance (DeFi) protocols carries significant risks. We strongly advise against using this code in production environments without thorough testing and validation.

Before using this code for actual transactions, please ensure you fully understand the associated risks and legal implications. The authors of this project are not responsible for any losses incurred due to the use of this software.



## ❤️ Acknowledgements

This project has been made possible thanks to the inspiration provided by the following projects:

- [ethers-kt](https://github.com/Kr1ptal/ethers-kt)