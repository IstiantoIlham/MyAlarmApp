package com.istianto.myalarmapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.istianto.myalarmapp.adapter.AlarmAdapter
import com.istianto.myalarmapp.room.AlarmDB
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var alarmAdapter: AlarmAdapter

    private lateinit var alarmReceiver: AlarmReceiver
    val db by lazy {AlarmDB(this)}

    override fun onResume() {
        super.onResume()
        db.alarmDao().getAlarm().observe(this@MainActivity) {
            alarmAdapter.setData(it)
            Log.d("MainActivity", "dbResponse: $it")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        alarmReceiver = AlarmReceiver()

        initTimeToday()
        initDateToday()
        initAlarmType()

        initRecycleView()
    }

    private fun initRecycleView() {
        alarmAdapter = AlarmAdapter()
        rv_reminder_alarm.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = alarmAdapter

            swipeToDelete(this)
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        ItemTouchHelper(object :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val typeOfAlarm = alarmAdapter.alarms[viewHolder.adapterPosition].type
                alarmReceiver.cancleAlarm(this@MainActivity, typeOfAlarm)

                val deleteItem = alarmAdapter.alarms[viewHolder.adapterPosition]

                //delete item
                CoroutineScope(Dispatchers.IO).launch {
                    db.alarmDao().deletAlarm(deleteItem)
                }
                alarmAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                Toast.makeText(applicationContext, "Succes Delete Alarm", Toast.LENGTH_LONG).show()
            }

        }).attachToRecyclerView(recyclerView)

    }

    private fun initAlarmType() {
        view_set_one_time_alarm.setOnClickListener{
            startActivity(Intent(this,OneTimeAlarmActivity::class.java))
        }
        view_set_repeating_alarm.setOnClickListener{
            startActivity(Intent(this,RepeatingAlarmActivity::class.java))
        }
    }

    private fun initDateToday() {
        val dateNow = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("E, dd MMM yyyy",Locale.getDefault())
        val formatedDate =dateFormat.format(dateNow.time)

        tv_date_today.text = formatedDate
    }

    private fun initTimeToday() {
        val timeNow = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm")
        val formatedTime = timeFormat.format(timeNow.time)

        tv_time_today.text = formatedTime
    }
}