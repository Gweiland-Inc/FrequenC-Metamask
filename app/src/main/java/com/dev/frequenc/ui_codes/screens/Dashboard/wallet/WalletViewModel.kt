package com.dev.frequenc.ui_codes.screens.Dashboard.wallet

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.metamask.androidsdk.Dapp
import io.metamask.androidsdk.ErrorType
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.EthereumMethod
import io.metamask.androidsdk.EthereumRequest
import io.metamask.androidsdk.EthereumState
import io.metamask.androidsdk.Logger
import io.metamask.androidsdk.Network
import io.metamask.androidsdk.RequestError
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
//    ethereumState?.value?.selectedAddress?.isNotEmpty()
    private val __connected = MutableLiveData<Boolean> (false)
    val connectedVals: LiveData<Boolean>
    get() = __connected

    // Wrapper function to connect the dapp
    fun connect(dapp: Dapp, callback: ((Any?) -> Unit)?) {
        ethereum.connect(dapp, callback)
    }

    fun disconnect() {
        ethereum.disconnect()
    }

    // Wrapper function call all RPC methods
    fun sendRequest(callback: ((Any?) -> Unit)?) {
        val message =
            "{\"domain\":{\"chainId\":\"${ethereum.chainId}\",\"name\":\"Ether Mail\",\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\",\"version\":\"1\"},\"message\":{\"contents\":\"Hello, Mahesh Babu !\",\"from\":{\"name\":\"Kinno\",\"wallets\":[\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\",\"0xDeaDbeefdEAdbeefdEadbEEFdeadbeEFdEaDbeeF\"]},\"to\":[{\"name\":\"Busa\",\"wallets\":[\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\",\"0xB0BdaBea57B0BDABeA57b0bdABEA57b0BDabEa57\",\"0xB0B0b0b0b0b0B000000000000000000000000000\"]}]},\"primaryType\":\"Mail\",\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Group\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"members\",\"type\":\"Person[]\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person[]\"},{\"name\":\"contents\",\"type\":\"string\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallets\",\"type\":\"address[]\"}]}}"
        val from = ethereum.selectedAddress
        val params: List<String> = listOf(from, message)

        val signRequest = EthereumRequest(
            method = EthereumMethod.ETH_SIGN_TYPED_DATA_V4.value,
            params = params
        )

        ethereum.sendRequest(signRequest, callback)
    }

    fun getBalence(callback: ((Any?) -> Unit)?) {
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


    fun sendTransaction(callback: ((Any?) -> Unit)?) {
        val from = ethereum.selectedAddress
        val to = "0x0000000000000000000000000000000000000000"
        val amount = "0x01"
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

    fun switchChain(
        chainId: String,
        onSuccess: (message: String) -> Unit,
        onError: (message: String, action: (() -> Unit)?) -> Unit
    ) {
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

    private fun addEthereumChain(
        chainId: String,
        onSuccess: (message: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        Logger.log("Adding chainId: $chainId")

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

    fun setConnectedVals( connectedVal: Boolean) {
        __connected.postValue(connectedVal)
    }

}