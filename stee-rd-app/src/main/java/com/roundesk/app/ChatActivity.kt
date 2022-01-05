package com.roundesk.app

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.socketio.client.Socket
import com.roundesk.sdk.activity.ApiFunctions
import com.roundesk.sdk.socket.SocketFunctions
import com.roundesk.sdk.util.LogUtil
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.ArrayList

class ChatActivity : AppCompatActivity(), View.OnClickListener,
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks  {

    private val TAG = ChatActivity::class.java.simpleName

    private var imgVideo: ImageView? = null
    private var txtCallerName: TextView? = null
    private var relLayTopNotification: RelativeLayout? = null
    private var btnAccept: Button? = null
    private var btnDecline: Button? = null
    private var mSocket: Socket? = null
    var arraylistReceiverId: ArrayList<String> = arrayListOf()
    private var callerId: String = "drpbzfjiouhqkaegcvtl"

    private val RC_CAMERA_PERM = 123
    private val RC_MICROPHONE_PERM = 124
    private val RC_STORAGE_PERM = 125

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Below line will initiate the Socket and make it open
        /*SocketFunctions(this).initiateSocket(
            mSocket,
            SocketConstants.SOCKET_SEND_CALL_TO_CLIENT + callerId,false
        )*/

        initView()

        ApiFunctions(this).getCallerRole(false)
    }

    private fun initView() {
        imgVideo = findViewById(R.id.imgVideo)
        relLayTopNotification = findViewById(R.id.relLayTopNotification)
        txtCallerName = findViewById(R.id.txtCallerName)
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)

        imgVideo?.setOnClickListener(this)
        btnAccept?.setOnClickListener(this)
        btnDecline?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgVideo -> {
                arraylistReceiverId.clear()
                arraylistReceiverId.add("agjqticlbhvredpouzkf")
                arraylistReceiverId.add("drpbzfjiouhqkaegcvtl")

                if (hasCameraPermission() && hasMicrophonePermission() && hasStoragePermission()) {

                    // Below line will initiate the call
                    ApiFunctions(this).initiateCall(
                        arraylistReceiverId,
                        "doctor",
                        "drpbzfjiouhqkaegcvtl",
                        "on",
                        "on",
                        getRandomString(9)
                    )
                } else {
                    if (!hasCameraPermission()) {
                        EasyPermissions.requestPermissions(
                            this,
                            getString(R.string.rationale_camera),
                            RC_CAMERA_PERM,
                            Manifest.permission.CAMERA
                        )
                    }

                    if (!hasMicrophonePermission()) {
                        EasyPermissions.requestPermissions(
                            this,
                            getString(R.string.rationale_microphone),
                            RC_MICROPHONE_PERM,
                            Manifest.permission.RECORD_AUDIO
                        )
                    }

                    if (!hasStoragePermission()) {
                        EasyPermissions.requestPermissions(
                            this,
                            getString(R.string.rationale_storage),
                            RC_STORAGE_PERM,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
            }

            R.id.btnAccept -> {
                relLayTopNotification?.visibility = View.GONE
            }
            R.id.btnDecline -> {
                relLayTopNotification?.visibility = View.GONE
            }
        }
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    private fun hasMicrophonePermission(): Boolean {
        return (EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO))
    }

    private fun hasStoragePermission(): Boolean {
        return (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                && EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        LogUtil.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        LogUtil.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        LogUtil.d(TAG, "onRationaleAccepted: $requestCode")
    }

    override fun onRationaleDenied(requestCode: Int) {
        LogUtil.d(TAG, "onRationaleDenied: $requestCode")
    }
}