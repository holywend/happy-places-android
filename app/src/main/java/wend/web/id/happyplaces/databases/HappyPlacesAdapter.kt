package wend.web.id.happyplaces.databases

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import wend.web.id.happyplaces.R
import wend.web.id.happyplaces.activities.AddPlacesActivity
import wend.web.id.happyplaces.activities.MainActivity
import wend.web.id.happyplaces.databinding.LinearlayoutRecyclerviewItemBinding


class HappyPlacesAdapter(private val items: ArrayList<HappyPlaceEntity>) :
    RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {
    class ViewHolder(binding: LinearlayoutRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val llPlaceItem = binding.llPlaceItem
        val ivPlace = binding.ivPlace
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
    }

    private lateinit var onClickListener: OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LinearlayoutRecyclerviewItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: HappyPlaceEntity = items[position]
        Log.e("onBindViewHolder", "\ncurrent pos: $position\n$item")
        holder.ivPlace.setImageURI(Uri.parse(item.image))
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.itemView.setOnClickListener {
            onClickListener.onClick(position, item)
        }
        if (position % 2 == 0) {
            holder.llPlaceItem.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.indigo_100)
            )
            holder.tvTitle.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        } else {
            holder.llPlaceItem.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.tvTitle.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.indigo_300)
            )
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // pass onClickListener method from other class
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, entity: HappyPlaceEntity)
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(activity, AddPlacesActivity::class.java)
        intent.putExtra(MainActivity.HAPPY_PLACE_ENTITY, items[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    suspend fun removeAt(context: Context, position: Int) {
        val happyPlaceDao = HappyPlaceDatabase.getInstance(context).happyPlaceDao()
        happyPlaceDao.delete(items[position])
        notifyItemRemoved(position)
    }
}