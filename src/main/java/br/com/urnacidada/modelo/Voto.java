package br.com.urnacidada.modelo;

import java.util.Objects;

/**
 * Voto dado por um eleitor para um cargo. Cada subclasse representa uma
 * situação possível na urna e sabe como se apresentar e se registrar.
 */
public abstract class Voto {

    private final Cargo cargo;

    protected Voto(Cargo cargo) {
        this.cargo = Objects.requireNonNull(cargo, "cargo");
    }

    public Cargo getCargo() {
        return cargo;
    }

    public abstract TipoVoto getTipo();

    /** Número registrado (candidato ou partido); vazio para brancos e nulos. */
    public abstract String getNumeroRegistrado();

    /** Mensagem de destaque exibida na tela antes da confirmação (pode ser vazia). */
    public abstract String getMensagemTela();

    /** Converte para o registro anônimo que vai para o banco de dados. */
    public RegistroVoto paraRegistro() {
        return new RegistroVoto(cargo.getNome(), getTipo(), getNumeroRegistrado());
    }
}
