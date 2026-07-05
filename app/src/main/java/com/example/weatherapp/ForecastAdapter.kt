package com.example.weatherapp

/*import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.model.ForecastItem

class ForecastAdapter(private var list: List<ForecastItem>) :
    RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.date)
        val temp: TextView = view.findViewById(R.id.temp)
        val icon: ImageView = view.findViewById(R.id.icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.date.text = item.dt_txt
        holder.temp.text = "${item.main.temp} °C"

        // Pobranie ikony z OpenWeather, bez crashu jeśli brak danych
        val iconCode = item.weather.firstOrNull()?.icon
        if (iconCode != null) {
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
            Glide.with(holder.icon.context).load(iconUrl).into(holder.icon)
        }
    }

    fun updateData(newList: List<ForecastItem>) {
        list = newList
        notifyDataSetChanged()
    }
}*/