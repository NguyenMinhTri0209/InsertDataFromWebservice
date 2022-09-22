package com.example.insertdatafromwebservice

import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    var mangKH: ArrayList<String> = ArrayList()
    var adaptertenKH: ArrayAdapter<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val urlGetdata: String = "http://192.168.1.12/webservice/getdata.php"
        var edtTen = findViewById<EditText>(R.id.editTen)
        var editHocPhi = findViewById<EditText>(R.id.editHocPhi)

        var urlinsert: String="http://192.168.1.12/webservice/insertdata.php"

        GetData().execute(urlGetdata)
        adaptertenKH = ArrayAdapter(this, android.R.layout.simple_list_item_1, mangKH)
        var lvCource = findViewById<ListView>(R.id.lvCourse)
        lvCource.adapter = adaptertenKH

        var btnThem = findViewById<Button>(R.id.btnThem)
        btnThem.setOnClickListener{
            var ten: String = edtTen.text.toString().trim()
            var hocPhi: String = editHocPhi.text.toString().trim()
            if (ten.length ==0 || hocPhi.length ==0){
                Toast.makeText(applicationContext, "Vui lòng nhập đủ thông tin", Toast.LENGTH_LONG).show()
            }
            else{
                insertData().execute(urlinsert)
                edtTen.setText("")
                editHocPhi.setText("")
                GetData().execute(urlGetdata)
            }
        }
    }

    inner class GetData : AsyncTask <String, Void, String>(){
        override fun doInBackground(vararg p0: String?): String {
            return getContentURL(p0[0])

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var jsonArray: JSONArray = JSONArray(result)
            var ten: String = ""
            var hp:String =""
            mangKH.clear()
            for (khoaHoc in 0..jsonArray.length()-1){
                var objectKH: JSONObject = jsonArray.getJSONObject(khoaHoc)
                ten = objectKH.getString("TenKH")
                hp = objectKH.getString("HocPhi")
                mangKH.add(ten + " - " + hp + "Đ")
            }
            adaptertenKH?.notifyDataSetChanged()
        }

        private fun getContentURL(url: String?) : String{
            var content: StringBuilder = StringBuilder();
            val url: URL = URL(url)
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            val inputStreamReader: InputStreamReader = InputStreamReader(urlConnection.inputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)

            var line: String = ""
            try {
                do {
                    line = bufferedReader.readLine()
                    if(line != null){
                        content.append(line)
                    }
                }while (line != null)
                bufferedReader.close()
            }catch (e: Exception){
                Log.d("AAA", e.toString())
            }
            return content.toString()
        }
    }

    inner class insertData : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg p0: String?): String {
            return postData(p0[0])
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result.equals("Success")){
                Toast.makeText(applicationContext, "Thêm thành công", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(applicationContext, "Thêm thất bại", Toast.LENGTH_LONG).show()
            }
        }

        private fun postData(link: String?): String {
            val connect: HttpURLConnection
            var url: URL =  URL(link)
            try {
                connect = url.openConnection() as HttpURLConnection
                connect.readTimeout = 10000
                connect.connectTimeout = 15000
                connect.requestMethod = "POST"
                // POST theo tham số
                var edtTen = findViewById<EditText>(R.id.editTen)
                var editHocPhi = findViewById<EditText>(R.id.editHocPhi)
                val builder = Uri.Builder()
                    .appendQueryParameter("tenkhoahoc",edtTen.text.toString().trim())
                    .appendQueryParameter("hocphiKH", editHocPhi.text.toString().trim())
                val query = builder.build().getEncodedQuery()
                val os = connect.outputStream
                val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                writer.write(query)
                writer.flush()
                writer.close()
                os.close()
                connect.connect()
            } catch (e1: IOException) {
                e1.printStackTrace()
                return "Error!"
            }

            try {
                // Đọc nội dung trả về sau khi thực hiện POST
                val response_code = connect.responseCode
                if (response_code == HttpURLConnection.HTTP_OK) {
                    val input = connect.inputStream
                    val reader = BufferedReader(InputStreamReader(input))
                    val result = StringBuilder()
                    var line: String
                    try {
                        do{
                            line = reader.readLine()
                            if(line != null){
                                result.append(line)
                            }
                        }while (line != null)

                        reader.close()
                    }catch (e:Exception){}

                    return result.toString()
                } else {
                    return "Error!"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return "Error!"
            } finally {
                connect.disconnect()
            }
        }
    }
}