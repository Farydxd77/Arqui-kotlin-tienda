package com.example.arquiprimerparcial.presentacion.adapter

import com.example.arquiprimerparcial.negocio.modelo.PedidoModelo

class PedidoAdapter(
    private val onClickListener: IOnClickListener
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    private var lista = emptyList<PedidoModelo>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    interface IOnClickListener {
        fun clickVerDetalle(pedido: PedidoModelo)
        fun clickEliminar(pedido: PedidoModelo)
    }

    inner class PedidoViewHolder(private val binding: ItemsPedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun enlazar(pedido: PedidoModelo) {
            binding.tvNumeroPedido.text = "#${pedido.id.toString().padStart(3, '0')}"
            binding.tvNombreCliente.text = pedido.nombreCliente
            binding.tvFecha.text = pedido.fechaPedido?.let { dateFormat.format(Date(it.time)) } ?: ""
            binding.tvTotal.text = "S/ ${pedido.formatearTotal()}"
            binding.tvCantidadProductos.text = "${pedido.cantidadTotalProductos()} productos"

            binding.root.setOnClickListener {
                onClickListener.clickVerDetalle(pedido)
            }

            binding.btnEliminar.setOnClickListener {
                onClickListener.clickEliminar(pedido)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        return PedidoViewHolder(
            ItemsPedidoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        holder.enlazar(lista[position])
    }

    fun setList(listaPedidos: List<PedidoModelo>) {
        this.lista = listaPedidos
        notifyDataSetChanged()
    }
}