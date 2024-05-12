package com.example.ethersktsniperbot.strategies

import UniswapV2Factory
import UniswapV2Pool
import UniswapV2Router02
import com.example.ethersktsniperbot.EthersKtSniperBotApplication
import io.ethers.core.isFailure
import io.ethers.core.types.Address
import io.ethers.core.types.BlockId
import io.ethers.providers.Provider
import io.ethers.providers.SubscriptionStream
import io.ethers.signers.PrivateKeySigner
import io.github.cdimascio.dotenv.Dotenv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

/**
 * This strategy subscribes to PairCreated emitted on Uniswap V2 Factory. When a new pair is created, we check the
 * reserves are greater than 0 and if one of the tokens is WETH. If the conditions are met, we send a transaction to
 * swap WETH for the other token in the pair.
 *
 * Note: This strategy is for educational purposes only and should not be used in production without further testing.
 * We wanted to showcase the capabilities of EthersKt and how to interact with Ethereum smart contracts. Implementation
 * of the trading logic is left to the reader.
 *
 * It is important to understand the risks involved in interacting with smart contracts and DeFi protocols.
 **/
@Component
class EthersKtSniperBotStrategy {
    private val logger = LoggerFactory.getLogger(EthersKtSniperBotApplication::class.java)

    // We need to provide websocket URL endpoint in order to subscribe to new blocks
    private val provider = Provider.fromUrl(Dotenv.load().get("ETH_RPC_URL")).unwrap()

    // Load the private key from the .env file to sign transactions
    private val signer = PrivateKeySigner(Dotenv.load().get("SIGNER_PRIVATE_KEY"))

    // Create a router instance to interact with Uniswap V2 Router02 contract
    private val router = UniswapV2Router02(provider, UNIV2_ROUTER_ADDRESS)

    /**
     * Start the strategy by initializing the subscription to Uniswap V2 Factory PairCreated events.
     **/
    final fun startEthersKtSniperBotStrategy() {
        logger.info("Subscribe to new NewPair creation events...")
        val filter = UniswapV2Factory.PairCreated.filter(provider)
        val stream =
            filter.subscribe()
                .sendAwait()
                .orElse {
                    UniswapV2Factory.PairCreated.filter(provider).watch().sendAwait()
                } // Fallback to polling if subscription is not supported
                .unwrap()

        monitorUniswapV2PairCreationFromMinedBlocks(stream)
    }

    /**
     * This method listens to new PairCreated events emitted by Uniswap V2 Factory contract. When a new pair is created,
     * we make appropriate checks and send a transaction to swap WETH for the other token in the pair.
     *
     * The method will run indefinitely until the thread is interrupted.
     **/
    private fun monitorUniswapV2PairCreationFromMinedBlocks(stream: SubscriptionStream<UniswapV2Factory.PairCreated>) {
        stream.forEachAsync {
            val pairAddress = it.pair
            val token0 = it.token0
            val token1 = it.token1
            logger.info(
                """
                Found tx (${it.transactionHash}) with NewPair event
                Block number: ${it.blockNumber}
                Factory address: ${it.address}
                Pair address: $pairAddress
                Token0: $token0
                Token1: $token1
                """.trimIndent(),
            )

            handlePairCreated(pairAddress, token0, token1)
        }
    }

    /**
     * This method handles the Uniswap V2 pair creation event by checking the reserves and sending a swap transaction.
     **/
    private fun handlePairCreated(
        pairAddress: Address,
        token0: Address,
        token1: Address,
    ) {
        // Create a Uniswap V2 pair instance to interact with the pair contract
        val pair = UniswapV2Pool(provider, pairAddress)

        // Get the reserves for the pair
        val poolReserves = pair.getReserves().call(BlockId.LATEST).sendAwait()
        if (poolReserves.isFailure()) {
            logger.error("Failed to get reserves for pool $pairAddress")
            return
        }
        val reserve0 = poolReserves.unwrap()._reserve0
        val reserve1 = poolReserves.unwrap()._reserve1

        logger.info("New UniV2Pair $pairAddress token0: $token0, reserveO: $reserve0, token1: $token1, reserve1: $reserve1")

        // Send a test transaction to swap WETH for token0
        // 1.) we check if reserves are greater than 0
        // 2.) we check if one of the tokens is WETH (assuming we only have WETH in the wallet)
        // 3.) we send a transaction to swap WETH for the other token
        if (reserve0 > BigInteger.ZERO && reserve1 > BigInteger.ZERO) {
            if (token0 == WETH_ADDRESS) {
                logger.info("Swap WETH for token1: $token1")
                // swapWethForToken(token1)
            } else if (token1 == WETH_ADDRESS) {
                logger.info("Swap WETH for token0: $token0")
                // swapWethForToken(token0)
            } else {
                logger.info("Pair $pairAddress does not contain WETH")
            }
        } else {
            logger.info("Reserves for UniV2Pair $pairAddress are 0")
        }
    }

    /**
     * This method sends a transaction to swap WETH for the given token.
     *
     * @param token The token to swap WETH for.
     **/
    private fun swapWethForToken(token: Address) {
        logger.info("Executing swapExactETHForTokens...")
        val deadline = ((System.currentTimeMillis() / 1000) + 1800).toBigInteger()
        val call =
            router.swapExactETHForTokens(
                BigInteger.ZERO, // amountOutMin
                arrayOf(WETH_ADDRESS, token), // path
                signer.address, // to
                deadline,
            )
        val pendingTx = call.value(TRADE_AMOUNT).send(signer).sendAwait().unwrap()

        println("Wait for transaction: ${pendingTx.hash} to be included in a block...")
        val receipt = pendingTx.awaitInclusion(retries = 10).unwrap()
        println("Buy tx ${receipt.transactionHash} was included in block ${receipt.blockNumber}")

        // We don't close the wsClient to keep listening to events
    }

    companion object {
        private val WETH_ADDRESS = Address("0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2")
        private val UNIV2_ROUTER_ADDRESS = Address("0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D")
        private val TRADE_AMOUNT = BigInteger.TEN.pow(16) // 0.01 ETH
    }
}
