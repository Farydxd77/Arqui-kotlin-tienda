package com.example.arquiprimerparcial.presentacion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.databinding.ItemsProductoBinding
import com.example.arquiprimerparcial.negocio.modelo.ProductoModelo

class ProductoAdapter(
    private val onClickListener: IOnClickListener
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private var lista = emptyList<ProductoModelo>()

    interface IOnClickListener {
        fun clickEditar(producto: ProductoModelo)
        fun clickEliminar(producto: ProductoModelo)
    }

    inner class ProductoViewHolder(private val binding: ItemsProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun enlazar(producto: ProductoModelo) {
            binding.tvTitulo.text = producto.descripcion
            binding.tvCodigoBarra.text = producto.codigobarra
            binding.tvPrecio.text = producto.formatearPrecio()

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

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.enlazar(lista[position])
    }

    fun setList(listaProducto: List<ProductoModelo>) {
        this.lista = listaProducto
        notifyDataSetChanged()
    }
}