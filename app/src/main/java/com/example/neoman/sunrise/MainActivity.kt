package com.example.neoman.sunrise

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    var place:String?=null
    var myIntent:Intent?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myIntent=Intent(this,InfoDisplay::class.java)
    }

    protected fun getInfo(view: View){
        place =City.text.toString()

        //use Yahoo Query Language, can change the * in urlStart to astronomy to get just sunrise and sunset info
        //or change to other result might want so less info to sort through in JSON response
        val apiURL="https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22"+
                place+
                "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys"
        MyAsyncTask().execute(apiURL)
    }


    inner class MyAsyncTask:AsyncTask<String, String, String>(){
        override fun onPreExecute() {

        }
        override fun doInBackground(vararg p0: String?): String {//could build apiURL here and make this an inner class
            try{
                val urlConnect= URL(p0[0]).openConnection() as HttpsURLConnection
                urlConnect.connectTimeout=7000
                val input=streamToString(urlConnect.inputStream)
                publishProgress(input)
            }
            catch(e:Exception){}
            return " "
        }

        fun streamToString(inStream:InputStream):String{
            val buffReader=BufferedReader(InputStreamReader(inStream) as Reader?)
            var input=""
            var line:String?=""
            try{
                while(line!=null){
                    line=buffReader.readLine()
                    input+=line
                }
                inStream.close()
            }catch(e:Exception){

            }
            return input
        }

        override fun onProgressUpdate(vararg values: String?) {//for some reason apiResponse stops after units doesn't include full json
            try{
                val apiResponse= JSONObject(values[0])
                val channel=apiResponse.getJSONObject("query").getJSONObject("results").getJSONObject("channel")
                val location=channel.getJSONObject("location")//city,region,country are properties of location
                val astronomy=channel.getJSONObject("astronomy")//sunrise and sunset are properties

                myIntent!!.putExtra("sunrise",astronomy.getString("sunrise"))
                myIntent!!.putExtra("sunset",astronomy.getString("sunset"))

                try{//location doesn't always have region according to API doc
                    myIntent!!.putExtra("loc",location.getString("city")+","+location.getString("region")+","+location.getString("country"))
                }
                catch(e:Exception){
                    myIntent!!.putExtra("loc",location.getString("city")+","+location.getString("country"))
                }
                //only switch if no errors in getting info into intent extras
                this@MainActivity.startActivity(myIntent)
            }
            catch (e:Exception){//API returned error or null results
                try{//check if it was error if so use toast to show error desc returned by API(not really good idea)
                    val apiResponse= JSONObject(values[0])
                    Toast.makeText(this@MainActivity,apiResponse.getString("error.description"), Toast.LENGTH_LONG).show()
                }
                catch(ex:Exception){//if not error than no results tell the no results for city
                    Toast.makeText(this@MainActivity,place+" returned no results", Toast.LENGTH_LONG).show()
                }
            }
        }

        override fun onPostExecute(result: String?) {

        }
    }
}
