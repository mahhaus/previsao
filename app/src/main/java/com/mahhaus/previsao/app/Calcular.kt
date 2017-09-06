package com.mahhaus.previsao.app

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import com.mahhaus.previsao.R
import com.mahhaus.previsao.domain.City
import com.mahhaus.previsao.domain.MhActivity
import io.realm.Realm
import java.io.*

/**
 * Created by josias on 04/09/17 - 13:53.
 *
 */
class Calcular : MhActivity() {
    private var TAG : String? =  this::class.java.simpleName
    private var mEditeTextNome: EditText? = null
    private var mEditeTextSobreNome: EditText? = null
    private var mEditeTextIdade: EditText? = null
    private var mButtonEnviar: Button? = null

    private var mRealm: Realm? = null
    private var mCities: String? = ""
    private var mListCities: ArrayList<City> = arrayListOf()

    override fun mapearComponentes() {
        mEditeTextNome = findViewById(R.id.edt_nome)
        mEditeTextSobreNome = findViewById(R.id.edt_sobrenome)
        mEditeTextIdade = findViewById(R.id.edt_idade)
        mButtonEnviar = findViewById(R.id.btn_enviar)
    }

    override fun acoesComponentes() {
        mButtonEnviar?.setOnClickListener({
            val nome = mEditeTextNome?.text
            val sobrenome = mEditeTextSobreNome?.text
            val idade = Integer.parseInt(mEditeTextIdade?.text.toString())

            toast("Nome: $nome $sobrenome \nIdade: $idade")
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calcular_activity)

        mRealm = Realm.getDefaultInstance()

        mapearComponentes()
        acoesComponentes()
        objectFromFile().execute(this)

        Log.e("tag", "awdwadaw")
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm?.close()
    }

    inner class objectFromFile : AsyncTask<Context, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg p0: Context?) : Void? {
            var filename = "city.json"
            try {
                val manager = p0[0]?.assets

                val inputStream = manager?.open(filename)

                if (inputStream != null) {
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    var stringBuilder = StringBuilder()

//                {
//                    "id": 519188,
//                    "name": "Novinki",
//                    "country": "RU",
//                    "coord": {
//                        "lon": 37.666668,
//                        "lat": 55.683334
//                    }
//                },

                    var receiveString = bufferedReader.readLine()
                    while (receiveString != null) {
                        stringBuilder.append(receiveString.trim())

                        if (receiveString.contains("\"lat\"")) {
                            var strObj = stringBuilder.toString()
                            stringBuilder = StringBuilder()

                            strObj = strObj.replace("[", "").replace("]", "").replace("}},", "").replace("{\"id", "\"id")

                            var gson = Gson()
                            try {
                                if (strObj.contains("\"BR\"")){
                                    mListCities.add(gson.fromJson("{" + strObj + "}}", City::class.java))
                                }
                            } catch (e: Exception){
                                Log.i(TAG,  "{" + strObj + "}}")
                                e.printStackTrace()
                            }
                        }


                        if (mListCities.size >= 5561) {
                            receiveString = null
                        } else {
                            receiveString = bufferedReader.readLine()
                        }

                    }

                    inputStream.close()
                }
            } catch (e: FileNotFoundException) {
                Log.e("activity", "File not found: " + e.toString())
            } catch (e: IOException) {
                Log.e("activity", "Can not read file: " + e.toString())
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            var listBr: ArrayList<City> = arrayListOf()
            mListCities.forEach { city: City ->
                listBr.add(city)
                mRealm?.executeTransaction { realm -> realm.copyToRealm(city) }
            }

            var gson = Gson()
            var CityBr = gson.toJson(listBr)
            writeToFile(CityBr)
        }
    }

    private fun writeToFile(data: String) {
        try {
            val outputStreamWriter = OutputStreamWriter(openFileOutput("br_city.txt", Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }

    }

    private fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun getWeather(key: String, latitude: Double, longitude: Double, time: Long, units: String, lang: String) {

    }

}