package br.com.urnacidada.persistencia;

import br.com.urnacidada.modelo.RegistroVoto;
import br.com.urnacidada.modelo.TipoVoto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoriosTest {

    private static final List<RegistroVoto> ELEITOR = List.of(
            new RegistroVoto("Vereador", TipoVoto.NOMINAL, "14111"),
            new RegistroVoto("Prefeito", TipoVoto.BRANCO, ""));

    @Test
    void arquivoJsonPersisteEntreInstancias(@TempDir Path pasta) throws Exception {
        Path arquivo = pasta.resolve("votos.json");
        new ArquivoJsonVotoRepository(arquivo).salvar(ELEITOR);
        new ArquivoJsonVotoRepository(arquivo).salvar(ELEITOR);

        List<RegistroVoto> lidos = new ArquivoJsonVotoRepository(arquivo).listarTodos();
        assertEquals(4, lidos.size());
        assertTrue(lidos.containsAll(ELEITOR));

        new ArquivoJsonVotoRepository(arquivo).zerar();
        assertEquals(0, new ArquivoJsonVotoRepository(arquivo).listarTodos().size());
    }

    @Test
    void arquivoInexistenteEhListaVazia(@TempDir Path pasta) throws Exception {
        assertEquals(List.of(), new ArquivoJsonVotoRepository(pasta.resolve("nada.json")).listarTodos());
    }

    @Test
    void reservaGuardaVotoQuandoPrincipalFalha() throws Exception {
        VotoRepository foraDoAr = new VotoRepository() {
            @Override
            public void salvar(List<RegistroVoto> v) throws PersistenciaException {
                throw new PersistenciaException("sem internet");
            }

            @Override
            public List<RegistroVoto> listarTodos() throws PersistenciaException {
                throw new PersistenciaException("sem internet");
            }

            @Override
            public void zerar() {
            }

            @Override
            public String getDescricao() {
                return "Nuvem";
            }
        };
        MemoriaVotoRepository reserva = new MemoriaVotoRepository();
        List<String> avisos = new ArrayList<>();
        VotoRepository repo = new VotoRepositoryComReserva(foraDoAr, reserva, avisos::add);

        repo.salvar(ELEITOR);
        assertEquals(ELEITOR, reserva.listarTodos());
        assertEquals(ELEITOR, repo.listarTodos());
        assertEquals(2, avisos.size());
    }

    @Test
    void apuracaoSomaPrincipalEReserva() throws Exception {
        MemoriaVotoRepository principal = new MemoriaVotoRepository();
        MemoriaVotoRepository reserva = new MemoriaVotoRepository();
        principal.salvar(ELEITOR);
        reserva.salvar(ELEITOR);
        VotoRepository repo = new VotoRepositoryComReserva(principal, reserva, m -> { });
        assertEquals(4, repo.listarTodos().size());
        repo.zerar();
        assertEquals(0, repo.listarTodos().size());
    }
}
