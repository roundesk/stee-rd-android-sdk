package com.roundesk_stee_sdk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.roundesk.sdk.R
import com.roundesk_stee_sdk.dataclass.CallHistoryResponseDataClass
import com.roundesk_stee_sdk.util.LogUtil
import java.text.SimpleDateFormat
import java.util.*

class CallHistoryAdapter(private val mContext: Context, private val mList: List<CallHistoryResponseDataClass?>) : RecyclerView.Adapter<CallHistoryAdapter.ViewHolder>() {
  
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view 
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_history, parent, false)
  
        return ViewHolder(view)
    }
  
    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        LogUtil.e("onBindViewHolder", "onBindViewHolder: ${mList.size}}")

        holder.txtCallerName.text = mList[position]!!.user[0].name
//        holder.txtTimeStamp.text = convertToCustomFormat(mList[position]!!.date)
        holder.txtTimeStamp.text = mList[position]!!.date

        if(mList[position]?.type.equals("outgoing", ignoreCase = true)){
            holder.txtCallStatus.text = "Dial"
            holder.txtCallStatus.setTextColor(ContextCompat.getColor(mContext, R.color.call_dial_color))
            holder.txtCallerName.setTextColor(ContextCompat.getColor(mContext, R.color.call_dial_color))
        }

        if(mList[position]?.type.equals("incoming", ignoreCase = true)){
            holder.txtCallStatus.text = "Received"
            holder.txtCallStatus.setTextColor(ContextCompat.getColor(mContext, R.color.call_received_color))
            holder.txtCallerName.setTextColor(ContextCompat.getColor(mContext, R.color.call_received_color))
        }

        if(mList[position]?.type.equals("missed", ignoreCase = true)){
            holder.txtCallStatus.text = "Missed"
            holder.txtCallStatus.setTextColor(ContextCompat.getColor(mContext, R.color.call_missed_color))
            holder.txtCallerName.setTextColor(ContextCompat.getColor(mContext, R.color.call_missed_color))
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCallerName: TextView = itemView.findViewById(R.id.txtCallerName)
        val txtTimeStamp: TextView = itemView.findViewById(R.id.txtTimeStamp)
        val txtCallStatus: TextView = itemView.findViewById(R.id.txtCallStatus)
        val imgCall: ImageView = itemView.findViewById(R.id.imgCall)
    }

    private fun convertToCustomFormat(dateStr: String?): String {
        val utc = TimeZone.getTimeZone("UTC")
//        val sourceFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
        val sourceFormat = SimpleDateFormat("yyyy-MMM-dd HH:mm:ss zzz")
        val destFormat = SimpleDateFormat("MMMM, dd HH:mm aa")
        sourceFormat.timeZone = utc
        val convertedDate = sourceFormat.parse(dateStr)
        return destFormat.format(convertedDate)
    }
}