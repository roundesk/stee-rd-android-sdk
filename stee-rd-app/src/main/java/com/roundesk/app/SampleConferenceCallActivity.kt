package com.roundesk.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*


class SampleConferenceCallActivity : AppCompatActivity() {

    private var relLayUser12: RelativeLayout? = null
    private var linlayUser34: LinearLayout? = null
    private var txtUser1: TextView? = null
    private var txtUser2: TextView? = null
    private var txtUser3: TextView? = null
    private var txtUser4: TextView? = null
    private var txtUser5: TextView? = null
    private var view: View? = null
    private var btnAddUser: Button? = null
    private var usersJoined: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_conference_call)

        relLayUser12 = findViewById(R.id.relLayUser12)
        linlayUser34 = findViewById(R.id.linlayUser34)
        txtUser1 = findViewById(R.id.txtUser1)
        txtUser2 = findViewById(R.id.txtUser2)
        txtUser3 = findViewById(R.id.txtUser3)
        txtUser4 = findViewById(R.id.txtUser4)
        txtUser5 = findViewById(R.id.txtUser5)
        btnAddUser = findViewById(R.id.btnAddUser)
        view = findViewById(R.id.view)
        relLayUser12?.visibility = View.GONE
        linlayUser34?.visibility = View.GONE
        txtUser5?.visibility = View.GONE
        multipleView()

        btnAddUser?.setOnClickListener {
            if (usersJoined == 5) {
                usersJoined = 2
            } else {
                usersJoined += 1
            }
            multipleView()
        }

        txtUser2?.setOnClickListener{
            flipView()
        }
    }


    private fun multipleView() {
        when (usersJoined) {
            2 -> {
                showDefaultView()
            }
            3 -> {
                showThreeUsersUI()
            }
            4 -> {
                showFourUsersUI()
            }
            5 -> {
                showFiveUsersUI()
            }
        }
    }

    private fun showDefaultView() {
        relLayUser12?.visibility = View.VISIBLE
        linlayUser34?.visibility = View.GONE
        txtUser5?.visibility = View.GONE
        view?.visibility = View.GONE

        val paramsCaller: RelativeLayout.LayoutParams =
            txtUser1?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsCaller.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.marginEnd = 0
        paramsCaller.topMargin = 0
        txtUser1?.layoutParams = paramsCaller

        val paramsReceiver: RelativeLayout.LayoutParams =
            txtUser2?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsReceiver.height = 510
        paramsReceiver.width = 432
        paramsReceiver.marginEnd = 48
        paramsReceiver.topMargin = 48
        paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        txtUser2?.layoutParams = paramsReceiver
        txtUser2?.elevation = 2F
    }

    private fun showThreeUsersUI() {
        twoUsersTopView()
        linlayUser34?.visibility = View.VISIBLE
        txtUser4?.visibility = View.GONE
    }

    private fun showFourUsersUI() {
        twoUsersTopView()
        linlayUser34?.visibility = View.VISIBLE
        txtUser4?.visibility = View.VISIBLE
    }

    private fun showFiveUsersUI() {
        twoUsersTopView()
        linlayUser34?.visibility = View.VISIBLE
        txtUser4?.visibility = View.VISIBLE
        txtUser5?.visibility = View.VISIBLE
    }

    private fun twoUsersTopView() {
        view?.visibility = View.VISIBLE
        val paramsCaller: RelativeLayout.LayoutParams =
            txtUser1?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsCaller.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.marginEnd = 0
        paramsCaller.topMargin = 0
        paramsCaller.addRule(RelativeLayout.ALIGN_PARENT_START)
        paramsCaller.addRule(RelativeLayout.START_OF, R.id.view)

        txtUser1?.layoutParams = paramsCaller

        val paramsReceiver: RelativeLayout.LayoutParams =
            txtUser2?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsReceiver.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsReceiver.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsReceiver.marginEnd = 0
        paramsReceiver.topMargin = 0
        paramsReceiver.addRule(RelativeLayout.END_OF, R.id.view);
        paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_END);
        txtUser2?.layoutParams = paramsReceiver
        txtUser2?.elevation = 2F
    }

    private fun flipView(){
        relLayUser12?.visibility = View.VISIBLE
        linlayUser34?.visibility = View.GONE
        txtUser5?.visibility = View.GONE
        view?.visibility = View.GONE

        val paramsCaller: RelativeLayout.LayoutParams =
            txtUser2?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsCaller.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.width = FrameLayout.LayoutParams.MATCH_PARENT
        paramsCaller.marginEnd = 0
        paramsCaller.topMargin = 0
        txtUser2?.layoutParams = paramsCaller

        val paramsReceiver: RelativeLayout.LayoutParams =
            txtUser1?.getLayoutParams() as RelativeLayout.LayoutParams
        paramsReceiver.height = 510
        paramsReceiver.width = 432
        paramsReceiver.marginEnd = 48
        paramsReceiver.topMargin = 48
        paramsReceiver.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        txtUser1?.layoutParams = paramsReceiver
        txtUser1?.elevation = 2F
        txtUser1?.bringToFront()
    }
}