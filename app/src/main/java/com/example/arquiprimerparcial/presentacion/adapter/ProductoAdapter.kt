package com.example.arquiprimerparcial.presentacion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.data.model.ProductoModel
import com.example.arquiprimerparcial.databinding.ItemsProductoBinding

class ProductoAdapter(
    private val onClickListener: IOnClickListener
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private var lista = emptyList<ProductoModel>()

    interface IOnClickListener {
        fun clickEditar(producto: ProductoModel)
        fun clickEliminar(producto: ProductoModel)
    }

    inner class ProductoViewHolder(private val binding: ItemsProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun enlazar(producto: ProductoModel) {
            binding.tvTitulo.text = producto.descripcion
            binding.tvCodigoBarra.text = producto.codigobarra
            binding.tvPrecio.text = String.format("%.2f", producto.precio)
//            binding.tvPrecio.text = UtilsCommon.formatFromDoubleToString(producto.precio)

            binding.ibEditar.setOnClickListener { onClickListener.clickEditar(producto) }
            binding.ibEliminar.setOnClickListener { onClickListener.clickEliminar(producto) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        return ProductoViewHolder(
            ItemsProductoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.enlazar(lista[position])
    }

    fun setList(listaProducto: List<ProductoModel>) {
        this.lista = listaProducto
        notifyDataSetChanged()
    }
}