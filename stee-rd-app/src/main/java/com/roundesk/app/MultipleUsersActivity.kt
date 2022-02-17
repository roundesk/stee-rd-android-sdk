package com.roundesk.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.content.ContextCompat

class MultipleUsersActivity : AppCompatActivity() {
    private var constraintMain: ConstraintLayout? = null
    private val paramsMATCHMATCH =
        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    private var linLayMultipleUser: LinearLayout? = null
    private var linLay1: LinearLayout? = null
    private var linLay2: LinearLayout? = null
    private var user1: TextView? = null
    private var user2: TextView? = null
    private var user3: TextView? = null
    private var usersJoined: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_users)
        constraintMain = findViewById(R.id.constraintMain)
        linLayMultipleUser = LinearLayout(this)
        linLay1 = LinearLayout(this)
        linLay2 = LinearLayout(this)
        user1 = TextView(this)
        user2 = TextView(this)
        user3 = TextView(this)
        multipleView()
    }

    fun Int.toDp(context: Context): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
    ).toInt()

    private fun showDefaultView() {
        // User 1
        user1?.id = View.generateViewId()
        user1?.text = "User 1"
        user1?.layoutParams = paramsMATCHMATCH
        user1?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
        user1?.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_200))
        constraintMain?.addView(user1)

        val constraintSetUser1 = ConstraintSet()
        constraintSetUser1.clone(constraintMain)

        constraintSetUser1.connect(
            user1!!.id, ConstraintSet.START,
            constraintMain!!.id, ConstraintSet.START,
        )

        constraintSetUser1.connect(
            user1!!.id, ConstraintSet.END,
            constraintMain!!.id, ConstraintSet.END,
        )

        constraintSetUser1.applyTo(constraintMain)

        // User 2
        user2?.id = View.generateViewId()
        user2?.text = "User 2"
        val param = LayoutParams(144.toDp(this), 170.toDp(this))
        param.setMargins(0, 16.toDp(this), 16.toDp(this), 0)
        user2?.layoutParams = param
        user2?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
        user2?.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
        constraintMain?.addView(user2)

        val constraintSetUser2 = ConstraintSet()
        constraintSetUser2.clone(constraintMain)

        constraintSetUser2.connect(
            user2!!.id, ConstraintSet.END,
            constraintMain!!.id, ConstraintSet.END,
        )

        constraintSetUser2.connect(
            user2!!.id, ConstraintSet.TOP,
            constraintMain!!.id, ConstraintSet.TOP,
        )

        constraintSetUser2.applyTo(constraintMain)
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

    private fun showThreeUsersUI() {
        //-----
        /*linLayMultipleUser?.id = View.generateViewId()
        linLayMultipleUser?.layoutParams = paramsMATCHMATCH
        linLayMultipleUser?.orientation = LinearLayout.VERTICAL
        linLayMultipleUser?.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.call_received_color
            )
        )
        constraintMain?.addView(linLayMultipleUser)*/

        //-----
        /*linLay1?.id = View.generateViewId()
        val param = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        param.setMargins(5.toDp(this), 5.toDp(this), 5.toDp(this), 5.toDp(this))
        param.weight = 1F
        linLay1?.layoutParams = param
        linLay1?.orientation = LinearLayout.HORIZONTAL
        linLay1?.setBackgroundColor(ContextCompat.getColor(this, R.color.call_missed_color))
        constraintMain?.addView(linLay1)*/

        //-----
        /*linLay2?.id = View.generateViewId()
        val param1 = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0F)
        param1.setMargins(5.toDp(this), 5.toDp(this), 5.toDp(this), 5.toDp(this))
        linLay2?.layoutParams = param1
        linLay2?.orientation = LinearLayout.HORIZONTAL
        linLay2?.setBackgroundColor(ContextCompat.getColor(this, R.color.call_dial_color))
        constraintMain?.addView(linLay2)*/


        // User 1
        user1?.id = View.generateViewId()
        user1?.text = "User 1"
        user1?.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f)
        user1?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
        user1?.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_200))
        linLay1?.addView(user1)

        /*val constraintSetUser1 = ConstraintSet()
        constraintSetUser1.clone(constraintMain)

        constraintSetUser1.connect(
            user1!!.id, ConstraintSet.START,
            linLay1!!.id, ConstraintSet.START,
        )

        constraintSetUser1.connect(
            user1!!.id, ConstraintSet.END,
            linLay1!!.id, ConstraintSet.END,
        )

        constraintSetUser1.applyTo(constraintMain)*/



    }

    private fun showFourUsersUI() {


    }

    private fun showFiveUsersUI() {


    }


}