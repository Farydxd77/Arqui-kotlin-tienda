package com.example.arquiprimerparcial.presentacion.adapter

import android.graphics.Color
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
        fun clickSeleccionar(producto: ProductoModelo) = Unit // Default implementation
    }

    inner class ProductoViewHolder(private val binding: ItemsProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun enlazar(producto: ProductoModelo) {
            binding.tvTitulo.text = producto.nombre
            // ✨ Mejorado: mostrar categoría o "Sin categoría"
            binding.tvCategoria.text = when {
                producto.nombreCategoria.isNotEmpty() -> "🏷️ ${producto.nombreCategoria}"
                producto.idCategoria > 0 -> "🏷️ Categoría ID: ${producto.idCategoria}"
                else -> "🏷️ Sin categoría"
            }
            binding.tvStock.text = "Stock: ${producto.stock}"
            binding.tvPrecio.text = "S/ ${producto.formatearPrecio()}"

            // Color según estado del stock
            when {
                producto.sinStock() -> {
                    binding.tvStock.setTextColor(Color.RED)
                    binding.root.alpha = 0.6f
                }
                producto.stockBajo() -> {
                    binding.tvStock.setTextColor(Color.parseColor("#FF9800"))
                    binding.root.alpha = 0.8f
                }
                else -> {
                    binding.tvStock.setTextColor(Color.parseColor("#4CAF50"))
                    binding.root.alpha = 1.0f
                }
            }

            binding.ibEditar.setOnClickListener { onClickListener.clickEditar(producto) }
            binding.ibEliminar.setOnClickListener { onClickListener.clickEliminar(producto) }

            // Para selección en pedidos - hacer clickeable toda la card
            binding.root.setOnClickListener { onClickListener.clickSeleccionar(producto) }
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