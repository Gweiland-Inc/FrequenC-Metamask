package com.dev.frequenc.ui_codes.connect.chat

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.dev.frequenc.R
import com.dev.frequenc.databinding.FragmentAllChatUserBinding
import com.dev.frequenc.ui_codes.connect.Profile.ProfileFragment
import com.dev.frequenc.ui_codes.connect.VibesProfileList.ConnectionAdapter
import com.dev.frequenc.ui_codes.data.ConnectionResponse
import com.dev.frequenc.util.Constants

class AllChatUserFragment : Fragment(), ChatListAdapter.ItemListListener,
    ConnectionAdapter.ListAdapterListener {

    private var currentActivity: FragmentActivity? = null
    lateinit var binding: FragmentAllChatUserBinding
    lateinit var allChatListViewModel: AllChatListViewModel

    companion object {
        private const val ItemUserListLay = 1
        private const val ItemUserChatPendingListLay = 2
        private const val ItemUserChatRequestsLay: Int = 3

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllChatUserBinding.inflate(inflater, container, false)
        try {
            currentActivity = activity
        } catch (ex: Exception) {
            try {
                currentActivity = requireActivity()
            } catch (exss: Exception) {
                exss.printStackTrace()
            }
            ex.printStackTrace()
        }

        allChatListViewModel =
            ViewModelProvider(requireActivity())[AllChatListViewModel::class.java]
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            activity?.getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE)
        val connectionAdapter = ConnectionAdapter(ArrayList(3), this@AllChatUserFragment)
        binding.rvConnection.apply {
            adapter = connectionAdapter
        }
        val chatListAdapter =
            ChatListAdapter(ArrayList(3), ItemUserListLay, this@AllChatUserFragment)
        binding.rvChatUser.apply {
            adapter = chatListAdapter
        }

        activity?.runOnUiThread {
            allChatListViewModel.isConnectionTabSelected.observe(viewLifecycleOwner) {
                sharedPreferences?.getString(Constants.Authorization, null)?.let { token ->
                    showConnectionTab(it, token)
                }
            }
        }

        activity?.runOnUiThread {
            allChatListViewModel.isPendingSubTabSelected.observe(viewLifecycleOwner) {
                sharedPreferences?.getString(Constants.Authorization, null)?.let { token ->
                    showPendingRequestsSubTab(
                        it,
                        token
                    )
                }
            }
        }
        allChatListViewModel.isApiCalled.observe(viewLifecycleOwner) {
            if (it == true) {
            binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        activity?.runOnUiThread {
            allChatListViewModel.userListsData?.observe(viewLifecycleOwner) { it23 ->
                it23?.let {
                    if (allChatListViewModel.isConnectionTabSelected.value == true) {
                        chatListAdapter.refreshData(
                            it,
                            ItemUserListLay
                        )
                        binding.tvChatTag.text = "Chats (${allChatListViewModel.chatCount.value})"
                    } else {
                        if (allChatListViewModel.isPendingSubTabSelected.value == true) {
                            chatListAdapter.refreshData(
                                it,
                                ItemUserChatPendingListLay
                            )
                        } else {
                            chatListAdapter.refreshData(
                                it,
                                ItemUserChatRequestsLay
                            )
                        }
                        binding.tvCountMyrequests.text = allChatListViewModel.myRequestCount.value.toString()
                        binding.tvCountPending.text = allChatListViewModel.pendingRequestCount.value.toString()
                    }
                    try {
                        binding.tvRequestsTag.text = "Requests (${allChatListViewModel.requestCount.value})"
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }

        activity?.runOnUiThread {
            allChatListViewModel.connectionList.observe(viewLifecycleOwner) {
                try {
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
//                if (it.isNullOrEmpty()) {
//                    binding.connectionNotFoundLay.visibility = View.VISIBLE
//                    binding.rvConnection.visibility = View.INVISIBLE
//                    binding.tvConnectionTag.text = "Connection"
//                } else {
//                    binding.rvConnection.visibility = View.VISIBLE
//                    binding.connectionNotFoundLay.visibility = View.GONE
//                    binding.tvConnectionTag.text = "Connection (${it.size})"
//                }
                connectionAdapter.update(it)
            }
        }

        sharedPreferences?.getString(Constants.Authorization, null)?.let { token ->
            allChatListViewModel.callConnectionApi(token)
        }

        activity?.runOnUiThread {
            allChatListViewModel.isDataFound.observe(viewLifecycleOwner) {
                if (it) {
                    binding.dataNotFoundLay.noDataLay.visibility = View.INVISIBLE
                    binding.rvChatUser.visibility = View.VISIBLE
                } else {
                    binding.rvChatUser.visibility = View.INVISIBLE
                    binding.dataNotFoundLay.noDataLay.visibility = View.VISIBLE
                }
            }
        }

        binding.tvRequestsTag.setOnClickListener {
            allChatListViewModel.setConnectionTab(false)
        }
        binding.tvConnectionTag.setOnClickListener {
            allChatListViewModel.setConnectionTab(true)
        }

        binding.pendingTab.setOnClickListener {
            allChatListViewModel.setPendingTab(true)
        }
        binding.myRequestsTab.setOnClickListener {
            allChatListViewModel.setPendingTab(false)
        }

        allChatListViewModel.setConnectionTab(true)
    }

    private fun showPendingRequestsSubTab(
        toShowPendingRequestTab: Boolean,
        tokens: String
    ) {
        if (allChatListViewModel.isApiCalled.value != true) {
        if (allChatListViewModel.isConnectionTabSelected.value == false) {
            if (toShowPendingRequestTab) {
                allChatListViewModel.callMyRequestApi(tokens)
                binding.headPending.setTextColor(Color.parseColor("#8023EB"))
                binding.selectedHeadPending.visibility = View.VISIBLE

                binding.headMyrequests.setTextColor(Color.parseColor("#171A1F"))
                binding.selectedHeadMyrequests.visibility = View.INVISIBLE
            } else {
                allChatListViewModel.callMyRequestApi(tokens)
                binding.headMyrequests.setTextColor(Color.parseColor("#8023EB"))
                binding.selectedHeadMyrequests.visibility = View.VISIBLE


                binding.headPending.setTextColor(Color.parseColor("#171A1F"))
                binding.selectedHeadPending.visibility = View.INVISIBLE
            }
        }
        }
    }

    private fun showConnectionTab(
        toShowConnectionTab: Boolean,
        token: String
    ) {
        if (allChatListViewModel.isApiCalled.value != true) {
        if (toShowConnectionTab) {
            allChatListViewModel.callConnectionApi(token)
            allChatListViewModel.callMyRequestApi(token)
            binding.tvConnectionTag.setTextColor(Color.parseColor("#8023EB"))
            binding.tvChatTag.visibility = View.VISIBLE

            binding.tvRequestsTag.setTextColor(Color.parseColor("#171A1F"))
            binding.requestLay.visibility = View.INVISIBLE
        } else {
            allChatListViewModel.setPendingTab(true)
            binding.tvRequestsTag.setTextColor(Color.parseColor("#8023EB"))
            binding.requestLay.visibility = View.VISIBLE

            binding.tvConnectionTag.setTextColor(Color.parseColor("#171A1F"))
            binding.tvChatTag.visibility = View.INVISIBLE
        }
        }
    }

    override fun onClickAtConnection(item: ConnectionResponse) {
        Toast.makeText(context, "Chat Screen is under construction. ", Toast.LENGTH_SHORT).show()
    }

    override fun onItemClicked(itemPosition: Int, useType: Int, action: String) {

        when (useType) {
            ItemUserListLay -> {
                performClickAction(action, 0)
            }

            ItemUserChatPendingListLay -> {
                performClickAction(action, 0)
            }

            ItemUserChatRequestsLay -> {
                performClickAction(action, 0)
            }
        }
    }

    private fun performClickAction(action: String, item_id: Int) {
        when (action) {
            "goChat" -> {
                Toast.makeText(context, "Chat Screen is under construction. ", Toast.LENGTH_SHORT)
                    .show()
                try {
                    currentActivity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.flFragment, DemoChatFragment())
                        ?.commit()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            "goProfile" -> {
                try {
                    currentActivity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.flFragment, ProfileFragment())
                        ?.commit()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            "showMenu" -> {
                Toast.makeText(context, "Menu is not designed. ", Toast.LENGTH_SHORT).show()
            }

            "accept" -> {
                Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show()
            }

            "decline" -> {
                Toast.makeText(context, "Rejected. ", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
