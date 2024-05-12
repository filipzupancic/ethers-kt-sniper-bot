package com.example.ethersktsniperbot

import com.example.ethersktsniperbot.strategies.EthersKtSniperBotStrategy
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EthersKtSniperBotApplication

fun main(args: Array<String>) {
    runApplication<EthersKtSniperBotApplication>(*args)
    EthersKtSniperBotStrategy().startEthersKtSniperBotStrategy()
}
