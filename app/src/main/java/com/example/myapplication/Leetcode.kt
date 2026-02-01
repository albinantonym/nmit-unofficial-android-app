package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import org.json.JSONObject
import java.net.URL


class Leetcode : AppCompatActivity()  {
    lateinit var spin:Spinner
    lateinit var tail:TextView
    var difficulty='e'
    var n = 0
    var processing=true
    var over=false
    var meowjson=JSONObject("{}")
    var meow=""
    var w=0
    var h=0
    lateinit var lin:LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leetcode)
        val di = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(di)
        w=di.widthPixels
        h=di.heightPixels
        tail=TextView(this)
        tail.setBackgroundColor(Color.TRANSPARENT)
        tail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        tail.textAlignment=View.TEXT_ALIGNMENT_CENTER
        tail.setText("Loading...")
        tail.layoutParams=RelativeLayout.LayoutParams(
            w,
            200)

        lin=findViewById<LinearLayout>(R.id.lin)
        lin.addView(tail)
        spin = findViewById<Spinner>(R.id.spinner)
        val items = resources.getStringArray(R.array.Difficulty)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spin.adapter=(adapter)
        spin.setOnItemSelectedListener(
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View?, position: Int, id: Long) {
                val selectedItem = items[position]
                if (selectedItem=="Easy") {
                    difficulty = 'e'
                    Log.i("MyNum", "e")
                }
                else if(selectedItem=="Medium") {
                    difficulty = 'm'
                    Log.i("MyNum", "m")
                }
                else {
                    difficulty = 'h'
                    Log.i("MyNum", "h")
                }
                n=0
                lin.removeAllViews()
                tail.setText("Loading...")
                lin.addView(tail)
                over=false
                processing=true
                getstuff()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    return
        }})

        val  src=findViewById<ScrollView>(R.id.src)

        src.viewTreeObserver.addOnScrollChangedListener {
            val view = src.getChildAt(src.childCount - 1)
            val bottomDetector: Int = view.bottom - (src.height + src.scrollY)
            if (bottomDetector == 0 && !over && !processing) {
                getstuff()
            }
        }

        val back=findViewById<ImageButton>(R.id.back)
        back.setOnClickListener{
            finish()
        }

    }

    private fun getstuff() {
        Thread {
            try {
                val diff=difficulty
                n++
                meow=(URL("https://script.google.com/macros/s/AKfycbzM9yZthJbtqBeAibJBNnTAJM9x8GYMzVt2xUXeAYNRRoIPawdBuEVjV-bL3XLf6aenEQ/exec?difficulty=$difficulty&n=$n").readText())
                Log.i("MyNum",meow)
                this.runOnUiThread {
                    lin.removeView(tail)
                }
                if(meow=="Null" && difficulty==diff){
                    over=true
                    tail.setText("No More Questions Available")
                    this.runOnUiThread{
                        lin.addView(tail)
                    }
                }
                else if(difficulty==diff){
                    meowjson = JSONObject(meow)
                    for(i in 1..10){
                        if(meowjson.has("$i") && !over) {
                            makestuff(i)
                        }
                        else{
                            over=true
                            tail.setText("No More Questions Available")
                        }
                    }
                    this.runOnUiThread{
                        lin.addView(tail)
                    }
                }

            }catch (e: Exception){
                Log.i("MyNum",e.toString())
            }
        }.start()
    }


    fun makestuff(i:Int){
        processing=true
        val stuff=JSONObject(meowjson["$i"].toString())

        var lay = ConstraintLayout(this)
        lay.layoutParams= RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        val set = ConstraintSet()
        var t1= TextView(this)

        t1.layoutParams= RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        t1.setText(stuff["heading"].toString())
        t1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        t1.setMaxWidth(w-310)
        t1.setOnClickListener{
            val i= Intent("android.intent.action.VIEW", Uri.parse(stuff["sol"].toString()));
            startActivity(i);
        }
        t1.setId(View.generateViewId());
        this.runOnUiThread {
            lay.addView(t1)

            set.clone(lay)
            set.connect(t1.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 10)
            set.connect(t1.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 5)
            set.applyTo(lay)
        }
        var t2= TextView(this)

        t2.layoutParams= RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        t2.setText(stuff["question"].toString())
        t2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        t2.setMaxWidth(w-40)
        t2.setId(View.generateViewId());
        this.runOnUiThread {
            lay.addView(t2)

            set.clone(lay)
            set.connect(t2.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20)
            set.connect(t2.getId(), ConstraintSet.TOP, t1.id, ConstraintSet.BOTTOM, 10)

            set.applyTo(lay)
        }
        var b1= Button(this)
        b1.layoutParams= RelativeLayout.LayoutParams(
            250,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        b1.setText("Check Answer")
        b1.setTextSize(TypedValue.COMPLEX_UNIT_SP,15f)
        b1.setOnClickListener{
            val i= Intent("android.intent.action.VIEW", Uri.parse(stuff["sol"].toString()+"solutions"));
            startActivity(i);
        }
        b1.setId(View.generateViewId());
        var d= View(this)
        d.layoutParams= RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            3)
        d.setBackgroundColor(Color.rgb(160,32,240))
        this.runOnUiThread {
            lay.addView(b1)

            set.clone(lay)
            set.connect(b1.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 10)
            set.connect(b1.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 5)

            set.applyTo(lay)
        }
        if(stuff["photo"].toString()!="") {
            try {
                var img=ImageView(this)
                val connection = URL(stuff["photo"].toString()).openConnection()
                connection.setDoInput(true);
                connection.connect();

                val myBitmap = BitmapFactory.decodeStream(connection.getInputStream())
                img.setImageBitmap(myBitmap)
                img.layoutParams= RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)

                this.runOnUiThread {
                    img.setId(View.generateViewId());
                    lay.addView(img)
                    set.clone(lay)
                    set.connect(img.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 10)
                    set.connect(img.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 10)
                    set.connect(img.getId(), ConstraintSet.TOP, t2.id, ConstraintSet.BOTTOM, 5)

                    set.applyTo(lay)
                }
            }catch (e:Exception){
                Log.i("MyNum",e.toString())
            }
        }
        this.runOnUiThread {
            lin.addView(lay)
            lin.addView(d)
        }
        processing=false

    }

}
