package com.roundesk.sdk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk.sdk.dataclass.CreateCallSocketDataClass
import com.roundesk.sdk.dataclass.RoomDetailDataClassResponse
import com.roundesk.sdk.util.Constants
import com.roundesk.sdk.util.LogUtil

class BottomSheetUserListAdapter(
    private val mContext: Context,
    private val mRoomDetailDataList: List<RoomDetailDataClassResponse.Success>
) :
    RecyclerView.Adapter<BottomSheetUserListAdapter.ViewHolder>() {

    private var mCreateCallSocketData: CreateCallSocketDataClass? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bottom_sheet_users, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        LogUtil.e(
            "onBindViewHolder",
            "onBindViewHolder: ${Gson().toJson(mRoomDetailDataList)}}"
        )

        LogUtil.e(
            "onBindViewHolder",
            "mCreateCallSocketData: ${Gson().toJson(mCreateCallSocketData)}}"
        )

        if (mRoomDetailDataList[position].receiverId == Constants.CALLER_SOCKET_ID) {
            holder.txtRinging.visibility = View.GONE
            holder.progressBar.visibility = View.GONE
            holder.imgCallRejected.visibility = View.GONE
            holder.imgCallAccepted.visibility = View.GONE
        }

        holder.txtBottomUserName.text =
            firstLetterCaps(mRoomDetailDataList[position].name)

        LogUtil.e(
            "list user id  " + position + mRoomDetailDataList[position].receiverId,
            "socket receiver id " + position + mCreateCallSocketData?.receiverId
        )
        if (mCreateCallSocketData?.callerId != Constants.CALLER_SOCKET_ID) {
            holder.txtRinging.visibility = View.GONE
            holder.progressBar.visibility = View.GONE
            holder.imgCallRejected.visibility = View.GONE
            holder.imgCallAccepted.visibility = View.GONE
        }

        if (mCreateCallSocketData?.type == Constants.SocketSuffix.SOCKET_TYPE_ACCEPT_CALL) {
            if (mRoomDetailDataList[position].receiverId == mCreateCallSocketData?.receiverId) {
                holder.progressBar.visibility = View.GONE
                holder.txtRinging.visibility = View.GONE
                holder.imgCallRejected.visibility = View.GONE
                holder.imgCallAccepted.visibility = View.VISIBLE
            }
        }

        if (mCreateCallSocketData?.type == Constants.SocketSuffix.SOCKET_TYPE_REJECT_CALL) {
            if (mRoomDetailDataList[position].receiverId == mCreateCallSocketData?.receiverId) {
                holder.progressBar.visibility = View.GONE
                holder.txtRinging.visibility = View.VISIBLE
                holder.txtRinging.text = "Rejected"
                holder.imgCallRejected.visibility = View.VISIBLE
                holder.imgCallAccepted.visibility = View.GONE
            }
        }
    }

    fun manageUIVisibility(createCallSocketDataClass: CreateCallSocketDataClass) {
        mCreateCallSocketData = createCallSocketDataClass
        notifyDataSetChanged()
    }

    private fun firstLetterCaps(str: String): String {
        val words = str.split(" ").toMutableList()
        var output = ""
        for (word in words) {
            output += word.capitalize() + " "
        }
        output = output.trim()
        return output
    }

    override fun getItemCount(): Int {
        return mRoomDetailDataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtBottomUserName: TextView = itemView.findViewById(R.id.txtBottomUserName)
        val txtRinging: TextView = itemView.findViewById(R.id.txtRinging)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val imgCallRejected: ImageView = itemView.findViewById(R.id.imgCallRejected)
        val imgCallAccepted: ImageView = itemView.findViewById(R.id.imgCallAccepted)
    }


}