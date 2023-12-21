package com.dev.frequenc.ui_codes.screens.Dashboard.wallet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.dev.frequenc.databinding.FragmentWalletBinding
import com.dev.frequenc.ui_codes.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import io.metamask.androidsdk.Dapp
import io.metamask.androidsdk.ErrorType
import io.metamask.androidsdk.RequestError
import io.metamask.androidsdk.TAG


@AndroidEntryPoint
class WalletFragment : Fragment() {


    private lateinit var walletBinding: FragmentWalletBinding
    private val viewModel by viewModels<WalletViewModel>()
    val dapp = Dapp("Droid Dapp", "https://droiddapp.com")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        walletBinding = FragmentWalletBinding.inflate(inflater, container, false)


        return walletBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindHandlers()
        bindObservers()

    }

    private fun bindHandlers() {
        walletBinding.apply {
            btnConnect.setOnClickListener {
                viewModel.connect(dapp) { result ->
                    if (result is RequestError) {
                        Log.e(TAG, "Ethereum connection error: ${result.message}")
                    } else {
                        Log.d(TAG, "Ethereum connection result: $result")
                        viewModel.sendRequest { result2 ->
                            if (result2 is RequestError) {
                                Log.e(TAG, "Ethereum sign error: ${result2.message}")
                            } else {
                                Log.d(TAG, "Ethereum sign result: $result2")
                            }
                        }
                    }
                }
            }
            btnBuy.setOnClickListener {
                if (etAmount.text.toString().isNotEmpty()) {
                    viewModel.approveSmartContract() { result ->
                        if (result is RequestError) {
                            Log.d(TAG, "Ethereum transaction error: ${result.message}")
                        } else {
                            Log.d(TAG, "Ethereum transaction result: $result")
                        }
                    }
//                    viewModel.smartContractFun(etAmount.text.toString().toDouble(), "approve")
                }
            }
            btnBuyTokens.setOnClickListener {
                if (etAmount.text.toString().isNotEmpty()) {
                    viewModel.buyTokenSmartContract(etAmount.text.toString().toDouble()) { result ->
                        if (result is RequestError) {
                            Log.d(TAG, "Ethereum transaction error: ${result.message}")
                        } else {
                            Log.d(TAG, "Ethereum transaction result: $result")
                        }
                    }
//                    viewModel.smartContractFun(etAmount.text.toString().toDouble(), "buyTokens")
//                    try {
//                        val longValue = etAmount.text.toString().toDouble()
//                        Log.d("NumberFormat", "bindHandlers: $longValue")
//                    } catch (e: NumberFormatException) {
//                        Log.d("NumberFormat", "bindHandlers: $e")
//                    }
                }
            }
        }
    }

    private fun bindObservers() {
        viewModel.ethereumState.observe(viewLifecycleOwner) {
            if (it.selectedAddress.isNotEmpty()) {
                walletBinding.tvAddress.text = it.toString()
            }
        }
    }


    private fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
    }

}

