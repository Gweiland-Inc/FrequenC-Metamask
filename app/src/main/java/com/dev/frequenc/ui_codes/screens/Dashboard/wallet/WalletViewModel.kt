package com.dev.frequenc.ui_codes.screens.Dashboard.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.metamask.androidsdk.Dapp
import io.metamask.androidsdk.ErrorType
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.EthereumMethod
import io.metamask.androidsdk.EthereumRequest
import io.metamask.androidsdk.EthereumState
import io.metamask.androidsdk.Logger
import io.metamask.androidsdk.Network
import io.metamask.androidsdk.RequestError
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.RawTransaction
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject


@HiltViewModel
class WalletViewModel @Inject constructor(
    private val ethereum: Ethereum
) : ViewModel() {

    val ethereumState = MediatorLiveData<EthereumState>().apply {
        addSource(ethereum.ethereumState) { newEthereumState ->
            value = newEthereumState
        }
    }

    private val __connected = MutableLiveData<Boolean>(false)

    val connectedVals: LiveData<Boolean>
        get() = __connected

    // Wrapper function to connect the dapp
    fun connect(dapp: Dapp, callback: ((Any?) -> Unit)?) {
//        viewModelScope.launch {
            if (connectedVals.value == true) {
                ethereum.clearSession()
            }
            ethereum.connect(dapp, callback)
//        }
    }

    fun disconnect() {
        viewModelScope.launch {
            ethereum.disconnect()
        }
    }

    // Wrapper function call all RPC methods
    fun sendRequest(callback: ((Any?) -> Unit)?) {
        viewModelScope.launch {
//            val message =
//                "{\"domain\":{\"chainId\":\"${ethereum.chainId}\",\"name\":\"Ether Mail\",\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\",\"version\":\"1\"},\"message\":{\"contents\":\"Hello, Mahesh Babu !\",\"from\":{\"name\":\"Kinno\",\"wallets\":[\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\",\"0xDeaDbeefdEAdbeefdEadbEEFdeadbeEFdEaDbeeF\"]},\"to\":[{\"name\":\"Busa\",\"wallets\":[\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\",\"0xB0BdaBea57B0BDABeA57b0bdABEA57b0BDabEa57\",\"0xB0B0b0b0b0b0B000000000000000000000000000\"]}]},\"primaryType\":\"Mail\",\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Group\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"members\",\"type\":\"Person[]\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person[]\"},{\"name\":\"contents\",\"type\":\"string\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallets\",\"type\":\"address[]\"}]}}"
          val message =
                "{\"domain\":{\"chainId\":\"${ethereum.chainId}\",\"name\":\"Ether Mail\",\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\",\"version\":\"1\"},\"message\":{\"contents\":\"Login signature\",\"from\":{\"name\":\"Kinno\",\"wallets\":[\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"]},\"to\":[{\"name\":\"Busa\",\"wallets\":[\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\",\"0xB0BdaBea57B0BDABeA57b0bdABEA57b0BDabEa57\",\"0xB0B0b0b0b0b0B000000000000000000000000000\"]}]},\"primaryType\":\"Mail\",\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Group\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"members\",\"type\":\"Person[]\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person[]\"},{\"name\":\"contents\",\"type\":\"string\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallets\",\"type\":\"address[]\"}]}}"
//            val message =
//                "{\"domain\":{\"chainId\":\"${ethereum.chainId}\",\"name\":\"Ether Mail\",\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\",\"version\":\"1\"},\"message\":{\"contents\":\"Login signature\"},\"primaryType\":\"Mail\",\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Group\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"members\",\"type\":\"Person[]\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person[]\"},{\"name\":\"contents\",\"type\":\"string\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallets\",\"type\":\"address[]\"}]}}"
            val from = ethereum.selectedAddress
            val params: List<String> = listOf(from, message)

            val signRequest = EthereumRequest(
                method = EthereumMethod.ETH_SIGN_TYPED_DATA_V4.value,
                params = params
            )

            ethereum.sendRequest(signRequest, callback)
        }
    }

    fun getBalence(callback: ((Any?) -> Unit)?) {
        viewModelScope.launch {
//        val tokenContractAddress = '0x2f930D27e0502Ef690A418D03CF037d0509000c7'
//        val tokenContractAbi =
            // Create parameters
            val params: List<String> = listOf(
                ethereum.selectedAddress,
                "latest" // "latest", "earliest" or "pending" (optional)
            )

            // Create request
            val getBalanceRequest = EthereumRequest(
                method = EthereumMethod.ETH_GET_BALANCE.value,
                params = params
            )

            ethereum.sendRequest(getBalanceRequest, callback)

//        {
//                result ->
//            if (result is RequestError) {
//                callback
//            } else {
//                balance = result.toString()
//            }
//        }
        }
    }

    // we are encoding the smartcontract function name along with the inputs,
    // we are using the encoded data to call the smartcontract function with metamask
    fun smartContractFun() {

        val from = ethereum.selectedAddress
        val contractAddress = "0x2f930D27e0502Ef690A418D03CF037d0509000c7"
        val value = BigInteger.valueOf(0.001.toLong())

        val function = Function(
            "functionName",
            listOf(),
            listOf()
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val transaction = Transaction.createFunctionCallTransaction(
            from,
            null,
            null,
            DefaultGasProvider.GAS_LIMIT,
            contractAddress,
            value,
            encodedFunction,
        )

        val transactionResponse = web3j.ethSendTransaction(transaction).sendAsync().get();

        val transactionHash: String = transactionResponse.transactionHash
    }

    fun sendTransaction(callback: ((Any?) -> Unit)?) {
        viewModelScope.launch {
            val from = ethereum.selectedAddress
//            val to = "0x0000000000000000000000000000000000000000"
            val to = "0x2f930D27e0502Ef690A418D03CF037d0509000c7"
            val amount = "0x5"
            val params: Map<String, Any> = mapOf(
                "from" to from,
                "to" to to,
                "amount" to amount
            )

            ethereum.sendRequest(
                EthereumRequest(
                    method = EthereumMethod.ETH_SEND_TRANSACTION.value,
                    params = listOf(params)
                ), callback
            )
//        { result ->
//            if (result is RequestError) {
//                // handle error
//            } else {
//                Log.d(TAG, "Ethereum transaction result: $result")
//            }
//        }
        }
    }

    fun switchChain(
        chainId: String,
        onSuccess: (message: String) -> Unit,
        onError: (message: String, action: (() -> Unit)?) -> Unit
    ) {
        viewModelScope.launch {
            val switchChainParams: Map<String, String> = mapOf("chainId" to chainId)
            val switchChainRequest = EthereumRequest(
                method = EthereumMethod.SWITCH_ETHEREUM_CHAIN.value,
                params = listOf(switchChainParams)
            )

            ethereum.sendRequest(switchChainRequest) { result ->
                if (result is RequestError) {
                    if (result.code == ErrorType.UNRECOGNIZED_CHAIN_ID.code || result.code == ErrorType.SERVER_ERROR.code) {
                        val message =
                            "${Network.chainNameFor(chainId)} ($chainId) has not been added to your MetaMask wallet. Add chain?"

                        val action: () -> Unit = {
                            addEthereumChain(
                                chainId,
                                onSuccess = { result ->
                                    onSuccess(result)
                                },
                                onError = { error ->
                                    onError(error, null)
                                }
                            )
                        }
                        onError(message, action)
                    } else {
                        onError("Switch chain error: ${result.message}", null)
                    }
                } else {
                    onSuccess("Successfully switched to ${Network.chainNameFor(chainId)} ($chainId)")
                }
            }

        }
    }

    private fun addEthereumChain(
        chainId: String,
        onSuccess: (message: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        Logger.log("Adding chainId: $chainId")
        viewModelScope.launch {
            val addChainParams: Map<String, Any> = mapOf(
                "chainId" to chainId,
                "chainName" to Network.chainNameFor(chainId),
                "rpcUrls" to Network.rpcUrls(Network.fromChainId(chainId))
            )
            val addChainRequest = EthereumRequest(
                method = EthereumMethod.ADD_ETHEREUM_CHAIN.value,
                params = listOf(addChainParams)
            )

            ethereum.sendRequest(addChainRequest) { result ->
                if (result is RequestError) {
                    onError("Add chain error: ${result.message}")
                } else {
                    if (chainId == ethereum.chainId) {
                        onSuccess("Successfully switched to ${Network.chainNameFor(chainId)} ($chainId)")
                    } else {
                        onSuccess("Successfully added ${Network.chainNameFor(chainId)} ($chainId)")
                    }
                }
            }
        }
    }

    fun setConnectedVals(connectedVal: Boolean) {
        viewModelScope.launch {
            if (connectedVal &&
                ethereumState?.value?.selectedAddress?.isNotEmpty() == true
            ) {
                __connected.value = (true)
            } else {
                __connected.value = (false)
            }
        }
    }

}