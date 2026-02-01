package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

//class TouchyDatePicker : DatePicker() {
//
//}

class MainActivity : AppCompatActivity() {
    //hello juniors(or anyone reading this), I'm Albin Antony M, was a student in CSE Department in NMIT.
    // If u want to contribute to solve any bugs or add any features, feel free to make a PR.

    private lateinit var webView: WebView //the web-view which displays all content
    private lateinit var reload:ImageButton //reload button which comes only when content doesn't load
    private lateinit var odd:Switch // odd portal or even wala switch
    val cookieManager = CookieManager.getInstance() //to use cookies in WebView

    override fun onCreate(savedInstanceState: Bundle?) {

        val preference=getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE) //preference will help store data long term(even after u close and reopen the app)
        var usn = preference.getString("USN","") //get usn if available

        if (usn=="") { // for new or logged out users
            super.onCreate(savedInstanceState)
            logoutstuff(preference) // function to load the get_details.xml
        }
        else {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            val back=findViewById<ImageButton>(R.id.back)
            back.setOnClickListener{
                finish() //goes sends u to previous intent(mostly entry.xml)
            }

            odd=findViewById<Switch>(R.id.switch1) //odd-even semester switch
            odd.setOnCheckedChangeListener{_,isChecked ->

                val editor = preference.edit()  //edit in preference to save the choice
                odd.setChecked(isChecked)
                editor.putBoolean("ODD", isChecked)
                editor.commit()
                dib(preference.getString("USN","").toString(),preference.getString("DOB","").toString()) //calling the dib(content loading function)
            }
            reload = findViewById<ImageButton>(R.id.reload)
            reload.visibility = (View.INVISIBLE)
            //reload button is invisible until any problem occurs(turn off ur internet and load the dib function to see it)
            val logout=findViewById<ImageButton>(R.id.logout)
            logout.setOnClickListener(){

                val editor = preference.edit()
                editor.putString("USN", "")//empties usn in preference
                editor.putString("DOB", "")//empties dob in preference
                editor.commit()
                logoutstuff(preference)
            }
            reload.setOnClickListener() {
                reload.visibility = (View.INVISIBLE)
                dib(preference.getString("USN","").toString(),preference.getString("DOB","").toString())
            }
            webView = findViewById(R.id.webview)
            webView.settings.setJavaScriptEnabled(true);
            webView.settings.domStorageEnabled=true
            //these below commented out lines where once used to debug many errors but not really needed now so yea
//            webView.settings.setLoadWithOverviewMode(true);
//            webView.settings.setUseWideViewPort(true);
//            webView.settings.setBuiltInZoomControls(true);
//            webView.settings.setDisplayZoomControls(false);
//            webView.settings.setSupportZoom(true);
//            webView.settings.setDefaultTextEncodingName("utf-8");
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true) //this line will cause any phone before Android 5.0 to crash but im lazy to add if condition
            webView.webViewClient = object : WebViewClient() {
                //needed to load every single thing in the webview and not redirect out
                override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                    view?.loadUrl(url)
                    return true
                }
            }
            webView.loadUrl("file:///android_asset/loading.html") //check assests folder (its some basic html-css code i copied from some site)
            odd.setChecked(false)
            if (preference.getBoolean("ODD",false)==true){
                odd.setChecked(true)
            }
            dib(preference.getString("USN","").toString(),preference.getString("DOB","").toString())
        }
    }
    private fun dib(usn:String ,dob:String) {
        //the most imp function to load the nmit parents portal content
        //dib initially had some meaning but i forgot abt it now
        Thread { //doing all post requests and stuff from different thread
            //trying to send post request from main thread crashes ur app, i learnt it the hard way
            try {// this is useful for the reload button as if anything fails i can turn on the reload button in catch block
                val time = Calendar.getInstance().time //unnecessary variable, i could directly put the thing in coming lines
                var formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") //our website uses this weird format, got it after reading their code
                val stuff = "contineo:" + formatter.format(time)
                //from here is the part to send post request
                var url = URL("https://nmitparents.contineo.in/")
                var urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true
                var postData = "parentkey=${Base64.encodeToString(stuff.toByteArray(Charsets.UTF_8),Base64.DEFAULT)}"
                var postDataBytes = postData.toByteArray(Charsets.UTF_8)
                urlConnection.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded"
                )
                urlConnection.setRequestProperty("Referer","https://nitte.edu.in/")
                urlConnection.setRequestProperty("Content-Length", postDataBytes.size.toString())
                val outputStream: OutputStream = BufferedOutputStream(urlConnection.outputStream)
//                Log.i("MyNumberApp", Base64.encodeToString(stuff.toByteArray(Charsets.UTF_8),Base64.DEFAULT).toString())
                outputStream.write(postDataBytes)
                outputStream.flush()
                var responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //if response is ok then this is done
                    val response = urlConnection.inputStream.bufferedReader().use { it.readText() }//response text is copied for scraping a value

                    val cookies = urlConnection.headerFields["Set-Cookie"]//saving the cookie they sent back for later
                    //the next searching for string algorithm was written before i learnt better ways to search for substring in a strin
                    //but it works so ok ig...
                    var ts="n\" value=\"\" readonly"//the stuff(substring) to search for in the response
                    //the following part is unnecessarily made confusing cuz i wanted to reuse some variables
                    //this is bad coding practice, always keep it simple and easy to understand
                    //also the variable names used here are rubbish, don't do this while ur coding, write clean code
                    var a = false //indicator whether i found the string or not
                    var j=0 // pointer variable which tracks the index of the substring(which is also used as a boolean after i find the string... ik unnecessarily complex but too lazy to change it)
                    for (i in response.iterator()) { //looping through main string
                        if (!a && j != 100 && i == ts[j]) { //if(im still searching for the substring and a character matches) then
                            j += 1 //increase the index(more like pointer to substring) of the substring
                            if (j == 20) { // 20 means the whole substring matched
                                a = true //a is made true
                                j = 0 //j is reset to 0(now just consider it as another boolean where ill make it 100 if its true and !100 if its false)
                            }
                        }
                        else if(j != 100 && a != true) { //if(im still searching for the substring and the letter didnt match)
                            j = 0 //turn j to 0 so as to restart searching from first index in substring(ts)
                        }
                        if (j == 100) { //if j(the boolean j) is true then update start storing the value we wanted to parse into ts(yea ts is reused again.. hehe)
                            if (i != '"') { //after the substring matches im search for more 3 '"' and then i start recording
                                //if u dont understand this part refer the website code, then ull understand why im doing what
                                ts = ts + i //basically recording the thing i wanted to scrape from page
                            } else
                                break // break once im done with recording(or should i say storing) the value to be scarped into ts
                        }
                        if (a == true) { //if(i have found the substring)
                            if (i == '"') {//then i look for " and keep searching for 3 of them
                                j = j + 1 //updating j counter to record how many i found till now
                                if (j == 3) { //once i find all " then..
                                    j = 100 // ..then i use j to mark that i should start recording(j acting as a booloan u see)
                                    a = false //making sure that i wont enter this current block again
                                    ts = "" // reusing the ts variable cuz i thought that it would be cool *face-palm*
                                }
                            }
                        }
                    }
                    // if u found the above rubbish irritating then here's the jist of it
                    // i wanted a substring from the response so i looped to get it and now i've got it in ts variable
//                    Log.i("MyNumberApp", ts )
                    //now i have coded the html page directly here(since it was easier than doing another post due to cookie problems)
                    var string = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Form</title>
</head>
<body>
<form id="loginForm" target="_self" action="https://nmitparents.contineo.in"""
                    if (odd.isChecked==true){
                        string+="/parentsodd"
                    }
                    string+="""/index.php" method="post" style="display:none;">
                    <input type="hidden" name="username" value=""""
                    string+= usn

                    string+="\">\n<input type=\"hidden\" name=\"dd\" value=\""+dob[8]+dob[9]
                    string+="\">\n<input type=\"hidden\" name=\"mm\" value=\""+dob[5]+dob[6]
                    string+="\">\n<input type=\"hidden\" name=\"yyyy\" value=\""+dob[0]+dob[1]+dob[2]+dob[3]
                    string+="\">\n" +
                            "    <input type=\"hidden\" name=\"passwd\" value=\""+dob
                    string+="\">\n" +
                            "    <input type=\"hidden\" name=\"option\" value=\"com_user\">\n" +
                            "    <input type=\"hidden\" name=\"task\" value=\"login\">\n" +
                            "    <input type=\"hidden\" name=\"return\" value=\"�w^Ƙi\">\n" +
                            "    <input type=\"hidden\" name=\"return\" value readonly>\n" +
                            "    <input type=\"hidden\" id=\"remember\" name=\"remember\" value=\"No\" alt=\"Remember Me\" readonly=\"\">\n"+
                            "    <input type=\"hidden\" name=\""+ts


                    string+="""" value="1">
</form>

<script>


    window.addEventListener("load", function() {
      const form = document.getElementById("loginForm");
      form.submit();
    });
  </script>
</body>
</html>"""
//read through the html if u want, its just a simple form
                    cookieManager.setCookie("https://nmitparents.contineo.in/",
                        cookies?.get(0) ?: "" //im using the cookie i saved earlier and setting it for my webVeiw
                        //so every request from that url will have this cookie attached
                    )
//                    Log.i("MyNumApp",string)
                    Handler(mainLooper).post{
                        //as this is running on a different thread than main thread so i need to shift back to
                        // main thread to make changes to things i made in main thread
                        reload.setVisibility(View.INVISIBLE)

//                        WebView.setWebContentsDebuggingEnabled(true) //this is really useful for debugging so look into it if u have any problem in the webview

                        webView.webViewClient = object : WebViewClient() {
                            // So our clg updated their code to make sure that only
                            // requests with Referer header gets approved,
                            // so i updated my code to match that
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                request?.url?.toString()?.let { redirectUrl ->
                                    val headers = mapOf(
                                        "Referer" to "https://nmitparents.contineo.in/"
                                    )
                                    view?.loadUrl(redirectUrl, headers)
                                }
                                return true // prevent default loading
                            }
                        }
                        //here im loading the string names "string" which i made into the webView,
                        // why was my naming so bad
                        webView.loadDataWithBaseURL("https://nmitparents.contineo.in/index.php",string, "text/html", "UTF-8",null)
                    }
                }
            } catch (e: Exception) {//if error happens
                Log.i("MyNumber", e.message.toString())//for debugging
                Handler(mainLooper).post{
                    reload.setVisibility(View.VISIBLE) //updated the visibility of reload button
                    webView.loadUrl("https://www.nmit.ac.in") //load a different site for no particular reason
                }
            }

        }.start()//starting the thread...
    }

    fun logoutstuff(preference: SharedPreferences) {
        setContentView(R.layout.get_details) //loads get_details.xml to enter the usn and dob
        val back=findViewById<ImageButton>(R.id.back)  //gets the back button object and stores it in back variable
        back.setOnClickListener{
            finish() //to go back to entry.xml when clicked on back
        }
        var USN = findViewById<EditText>(R.id.usn) //to get the EditText object from get_details.xml
        USN.setFocusableInTouchMode(true)
        fun Context.hideKeyboard(view: View){ //defining a function to hide keyboard
            // (the keyboard was not hiding automatically when clicked anywhere other than usn entry box soo...)
            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply{
                hideSoftInputFromWindow(view.windowToken,0)
            }
        }
        USN.setOnFocusChangeListener() { _, hasFocus -> //checking if USN entry box has focus
            if (!hasFocus) { //if it doesnt then hides it
                hideKeyboard(USN)
            }
        }

        var DOB = findViewById<DatePicker>(R.id.dob)
        findViewById<ScrollView>(R.id.sv).setOnTouchListener{ev,p-> //same thing but for DOB(since DOB click was not hiding it either after the eariler fix)
            if (!(ev is EditText)){hideKeyboard(USN)}
            false

        }
        DOB.viewTreeObserver.addOnScrollChangedListener{
            //in landscape mode to make scrolling easier (since the entire app was scrolling when trying to scroll the dob year)
            DOB.parent.parent.requestDisallowInterceptTouchEvent(true)
        }


        var submit = findViewById<ImageButton>(R.id.submit)
        fun DatePicker.getDate(): Date { //function to basically get date from the DatePicker in the get_entries.xml
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            return calendar.time
        }
        submit.setOnClickListener() { // self-explanatory


            val editor = preference.edit() //to change in preference which was made earlier
            editor.putString("USN", USN.getText().toString()) //puting usn
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            editor.putString("DOB", formatter.format(DOB.getDate())) //putting date in the format shown above
            editor.commit() //commits the changes to preference(imma just call it pref from now)

//            Log.i("MyNumberApp", formatter.format(DOB.getDate()))
//            Log.i("MyNumberApp", USN.getText().toString())
            setContentView(R.layout.activity_main) //load activity_main.xml
            val back=findViewById<ImageButton>(R.id.back)
            back.setOnClickListener{
                //doing this again since the layout changed so...
                //the earlier back button is not the same as this one
                finish()
            }
            odd=findViewById<Switch>(R.id.switch1) //odd-even semester portal switch
            odd.setOnCheckedChangeListener{_,isChecked ->

                val editor = preference.edit() //editing in pref to not have to click each time
                odd.setChecked(isChecked)
                editor.putBoolean("ODD", isChecked)
                editor.commit()
                dib(preference.getString("USN","").toString(),preference.getString("DOB","").toString()) //calling the dib(content loading function) function
            }
            reload = findViewById<ImageButton>(R.id.reload)
            reload.visibility = (View.INVISIBLE) //reload button isinvisible until any problem occurs(turn off ur internet and load the dib function to see it)
            val logout=findViewById<ImageButton>(R.id.logout)
            logout.setOnClickListener(){

                val editor = preference.edit()
                editor.putString("USN", "")
                editor.putString("DOB", "")
                editor.commit()
                logoutstuff(preference)
                //may seem that im recursively calling the function but no
                //since it runs onclick so im just setting up a link here
            }
            reload.setOnClickListener() {
            reload.visibility=View.INVISIBLE
                dib(
                    preference.getString("USN", "").toString(),
                    preference.getString("DOB", "").toString()
                )//yea, reload button basically tries to run the dib(content loading function) again
            }
            webView = findViewById(R.id.webview) //webview which shows the page
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true) //this thing can be run for oly above Android 5.0 but im too lazy to add if condition so let it cause a crash
            webView.settings.setJavaScriptEnabled(true)

            webView.webViewClient = object : WebViewClient() { //needed to load every single thing in the webview and not redirect out
                override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                    view?.loadUrl(url)
                    return true
                }
            }
            webView.loadUrl("file:///android_asset/loading.html") //check assests folder (its some basic html-css code i copied from some site)

            odd=findViewById<Switch>(R.id.switch1)
            odd.setChecked(false)
            if (preference.getBoolean("ODD",false)==true){
                odd.setChecked(true)
            }
            dib( // u must know by now, im too tired to type again
                preference.getString("USN", "").toString(),
                preference.getString("DOB", "").toString()
            )
        }
    }
    override fun onBackPressed() {
        //i wanted system back to take u to last webpage so...
        if (webView.canGoBack()) {
            // if possible go back in webview
            webView.goBack()
        } else {
            // if not possible just handle going back as always
            super.onBackPressed()
        }
    }

}
