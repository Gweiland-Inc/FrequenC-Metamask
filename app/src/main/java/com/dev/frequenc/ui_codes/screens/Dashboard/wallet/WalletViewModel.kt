package com.dev.frequenc.ui_codes.screens.Dashboard.wallet

import android.util.Log
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
import io.metamask.androidsdk.TAG
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
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

    // To call the sign method
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

    // Calling Approve Method, The function name "approve" is encoded along with it's input parameters
    //The encoded string is passed as data to the sendTransaction
    fun approveSmartContract(
        value: Double, // decimal value of type Double from editText eg. 0.0001
        callback: ((Any?) -> Unit)?
    ) {
        val function = Function(
            "approve",
            listOf(
                Address("0xC339BFCa084Df58DfAe10b111465057798C131f6"), // Replace with the spender's address
                Uint256(BigInteger.valueOf(value.toLong())) // Replace with the value you want to approve
            ),
            listOf(object : TypeReference<Bool>() {})
        )
        val encodedFunction = FunctionEncoder.encode(function)
        Log.d("Encoded", encodedFunction)

        // sendTransaction method
        viewModelScope.launch {
            val from = ethereum.selectedAddress
            val to = "0x90F96A3f63bbC7A9b30C90c31949055Ec304C484"

            val params: Map<String, Any> = mapOf(
                "from" to from,
                "to" to to,
                "data" to encodedFunction
            )
            ethereum.sendRequest(
                EthereumRequest(
                    method = EthereumMethod.ETH_SEND_TRANSACTION.value,
                    params = listOf(params)
                ), callback
            )
        }
    }

    // Calling buyTokens Method, The function name "buyTokens" is encoded along with it's input parameters
    //The encoded string is passed as data to the sendTransaction.
    fun buyTokenSmartContract(
        value: Double,
        callback: ((Any?) -> Unit)?
    ) {
        val function = Function(
            "buyTokens",
            listOf(
                Uint256(BigInteger.valueOf(value.toLong())) // Replace with the amount of tokens you want to buy
            ),
            listOf(object : TypeReference<Bool>() {})
        )
        val encodedFunction = FunctionEncoder.encode(function)
        Log.d("Encoded", encodedFunction)

        // sendTransaction method
        viewModelScope.launch {
            val from = ethereum.selectedAddress
            val to = "0xC339BFCa084Df58DfAe10b111465057798C131f6"

            val params: Map<String, Any> = mapOf(
                "from" to from,
                "to" to to,
                "value" to "1000000000000", // You can dynamically get the 'value', which is the
                // amount of ETH you want to process the transaction with
                "data" to encodedFunction
            )
            ethereum.sendRequest(
                EthereumRequest(
                    method = EthereumMethod.ETH_SEND_TRANSACTION.value,
                    params = listOf(params)
                ), callback
            )
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