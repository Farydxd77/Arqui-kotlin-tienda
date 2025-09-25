package com.example.arquiprimerparcial.presentacion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.databinding.ItemsPedidoDetalleBinding
import com.example.arquiprimerparcial.negocio.modelo.DetallePedidoModelo

class PedidoDetalleAdapter(
    private val onClickListener: IOnClickListener
) : RecyclerView.Adapter<PedidoDetalleAdapter.DetalleViewHolder>() {

    private var lista = mutableListOf<DetallePedidoModelo>()

    interface IOnClickListener {
        fun clickEliminar(detalle: DetallePedidoModelo)
        fun clickModificarCantidad(detalle: DetallePedidoModelo, nuevaCantidad: Int)
    }

    inner class DetalleViewHolder(private val binding: ItemsPedidoDetalleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun enlazar(detalle: DetallePedidoModelo) {
            binding.tvNombreProducto.text = detalle.nombreProducto
            binding.tvPrecioUnitario.text = "S/ ${detalle.formatearPrecioUnitario()}"
            binding.tvCantidad.text = detalle.cantidad.toString()
            binding.tvSubtotal.text = "S/ ${detalle.formatearSubtotal()}"

            binding.btnMenos.setOnClickListener {
                if (detalle.cantidad > 1) {
                    onClickListener.clickModificarCantidad(detalle, detalle.cantidad - 1)
                }
            }

            binding.btnMas.setOnClickListener {
                onClickListener.clickModificarCantidad(detalle, detalle.cantidad + 1)
            }

            binding.btnEliminar.setOnClickListener {
                onClickListener.clickEliminar(detalle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
        return DetalleViewHolder(
            ItemsPedidoDetalleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
        holder.enlazar(lista[position])
    }

    fun setList(nuevaLista: List<DetallePedidoModelo>) {
        this.lista = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    fun agregarDetalle(detalle: DetallePedidoModelo) {
        lista.add(detalle)
        notifyItemInserted(lista.size - 1)
    }

    fun eliminarDetalle(position: Int) {
        if (position in 0 until lista.size) {
            lista.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}