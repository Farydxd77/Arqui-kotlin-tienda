package com.example.arquiprimerparcial.presentacion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.databinding.ItemsCategoriaBinding
import com.example.arquiprimerparcial.negocio.modelo.CategoriaModelo

class CategoriaAdapter(
    private val onClickListener: IOnClickListener
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    private var lista = emptyList<CategoriaModelo>()

    interface IOnClickListener {
        fun clickEditar(categoria: CategoriaModelo)
        fun clickEliminar(categoria: CategoriaModelo)
    }

    inner class CategoriaViewHolder(private val binding: ItemsCategoriaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun enlazar(categoria: CategoriaModelo) {
            binding.tvNombre.text = categoria.nombre
            binding.tvDescripcion.text = if (categoria.descripcion.isNotEmpty()) {
                categoria.descripcion
            } else {
                "Sin descripción"
            }

            // Mostrar ID para referencia
            binding.tvId.text = "ID: ${categoria.id}"

            binding.ibEditar.setOnClickListener {
                onClickListener.clickEditar(categoria)
            }

            binding.ibEliminar.setOnClickListener {
                onClickListener.clickEliminar(categoria)
            }

            // Click en toda la card para editar
            binding.root.setOnClickListener {
                onClickListener.clickEditar(categoria)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        return CategoriaViewHolder(
            ItemsCategoriaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.enlazar(lista[position])
    }

    fun setList(listaCategoria: List<CategoriaModelo>) {
        this.lista = listaCategoria
        notifyDataSetChanged()
    }
}