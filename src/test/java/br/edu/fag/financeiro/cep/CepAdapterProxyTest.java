package br.edu.fag.financeiro.cep;

import br.edu.fag.financeiro.cep.adapter.BrasilApiAdapter;
import br.edu.fag.financeiro.cep.adapter.ViaCepAdapter;
import br.edu.fag.financeiro.cep.provedor.BrasilApiSimulado;
import br.edu.fag.financeiro.cep.provedor.ViaCepSimulado;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CepAdapterProxyTest {

    private static final Endereco SE = new Endereco("01001000", "Praça da Sé", "Sé", "São Paulo", "SP");

    // ---------- Adapter: formatos diferentes, mesmo Endereco ----------

    @Test
    void viaCepAdapterTraduzJsonDaViaCep() {
        ConsultaCep consulta = new ViaCepAdapter(new ViaCepSimulado());
        assertEquals(SE, consulta.buscar("01001000"));
    }

    @Test
    void brasilApiAdapterTraduzJsonDaBrasilApi() {
        ConsultaCep consulta = new BrasilApiAdapter(new BrasilApiSimulado());
        assertEquals(SE, consulta.buscar("01001000"));
    }

    @Test
    void doisProvedoresDevolvemOMesmoEnderecoPelaMesmaInterface() {
        ConsultaCep a = new ViaCepAdapter(new ViaCepSimulado());
        ConsultaCep b = new BrasilApiAdapter(new BrasilApiSimulado());
        assertEquals(a.buscar("89010025"), b.buscar("89010025"));
    }

    @Test
    void cepInexistenteViraCepNaoEncontradoNosDoisAdapters() {
        assertThrows(CepNaoEncontradoException.class,
                () -> new ViaCepAdapter(new ViaCepSimulado()).buscar("99999999"));
        assertThrows(CepNaoEncontradoException.class,
                () -> new BrasilApiAdapter(new BrasilApiSimulado()).buscar("99999999"));
    }

    @Test
    void viaCepComErroEmTextoTambemEhNaoEncontrado() {
        // versoes antigas da ViaCEP mandam {"erro": "true"}
        ConsultaCep consulta = new ViaCepAdapter(cep -> "{\"erro\": \"true\"}");
        assertThrows(CepNaoEncontradoException.class, () -> consulta.buscar("99999999"));
    }

    @Test
    void jsonQuebradoViraProvedorCepException() {
        assertThrows(ProvedorCepException.class, () -> new ViaCepAdapter(cep -> "<html>").buscar("01001000"));
        assertThrows(ProvedorCepException.class, () -> new BrasilApiAdapter(cep -> "nao e json {").buscar("01001000"));
    }

    // ---------- Proxy ----------

    /** Provedor falso que conta quantas vezes foi chamado. */
    private static class ProvedorContador implements ConsultaCep {
        final AtomicInteger chamadas = new AtomicInteger();

        @Override
        public Endereco buscar(String cep) {
            chamadas.incrementAndGet();
            return SE;
        }
    }

    @Test
    void proxyUsaCacheNaSegundaConsulta() {
        ProvedorContador provedor = new ProvedorContador();
        ConsultaCep proxy = new ConsultaCepProxy(provedor);

        assertEquals(SE, proxy.buscar("01001000"));
        assertEquals(SE, proxy.buscar("01001000"));
        assertEquals(SE, proxy.buscar("01001000"));

        assertEquals(1, provedor.chamadas.get(), "provedor so deve ser chamado na 1a vez");
    }

    @Test
    void proxyConsultaProvedorParaCepsDiferentes() {
        ProvedorContador provedor = new ProvedorContador();
        ConsultaCep proxy = new ConsultaCepProxy(provedor);

        proxy.buscar("01001000");
        proxy.buscar("89010025");

        assertEquals(2, provedor.chamadas.get());
    }

    @Test
    void proxyBloqueiaFormatoInvalidoSemChamarOProvedor() {
        ProvedorContador provedor = new ProvedorContador();
        ConsultaCep proxy = new ConsultaCepProxy(provedor);

        for (String ruim : new String[]{null, "", "123", "0100100", "010010000", "01001-000", "abcdefgh", "0100100a", " 1001000", "٠١٠٠١٠٠٠"}) {
            assertThrows(CepInvalidoException.class, () -> proxy.buscar(ruim), "deveria bloquear: " + ruim);
        }
        assertEquals(0, provedor.chamadas.get(), "provedor nao pode ser chamado com CEP invalido");
    }

    @Test
    void proxyNaoGuardaErroNoCache() {
        AtomicInteger chamadas = new AtomicInteger();
        ConsultaCep proxy = new ConsultaCepProxy(cep -> {
            chamadas.incrementAndGet();
            throw new CepNaoEncontradoException("CEP nao encontrado");
        });

        assertThrows(CepNaoEncontradoException.class, () -> proxy.buscar("99999999"));
        assertThrows(CepNaoEncontradoException.class, () -> proxy.buscar("99999999"));

        assertEquals(2, chamadas.get(), "erro nao deve ficar em cache");
    }

    @Test
    void proxyEAdapterJuntosComoNaAplicacao() {
        ConsultaCep proxy = new ConsultaCepProxy(new BrasilApiAdapter(new BrasilApiSimulado()));
        assertEquals(SE, proxy.buscar("01001000"));
        assertEquals(SE, proxy.buscar("01001000"));          // 2a vez: cache
        assertThrows(CepNaoEncontradoException.class, () -> proxy.buscar("99999999"));
        assertThrows(CepInvalidoException.class, () -> proxy.buscar("12"));
    }
}
