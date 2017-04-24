package br.edu.fatecsbc.sigapi.coletor.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openqa.selenium.By;

import br.edu.fatecsbc.sigapi.coletor.model.Login;
import br.edu.fatecsbc.sigapi.coletor.selenium.SigaClient;
import br.edu.fatecsbc.sigapi.coletor.selenium.SigaClient.PAGE;

@Service
public class ColetorService {

    private static final Logger log = LoggerFactory.getLogger(ColetorService.class);

    private static final String ARQUIVO_PARTICIPANTES = "participantes.csv";
    private static final String ENCODING = "UTF-8";
    private static final String PATTERN_EMAIL = "([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)";

    @Value("${sigapi.coletor.diretorio:/tmp/sigapi/coletor}")
    private String caminhoDiretorioRaiz;

    public boolean connect(final Login login) {

        if (login == null) {
            log.warn("Login não informado");
            return false;
        }

        final String usuario = login.getUsuario();
        final String senha = login.getSenha();

        SigaClient client = null;
        try {
            client = connect(usuario, senha);
        } finally {
            if (client != null) {
                client.quit();
            }
        }

        return client != null;

    }

    private SigaClient connect(final String usuario, final String senha) {

        log.info("Tentando conexão com o usuário '{}'", usuario);

        if (StringUtils.isAnyBlank(usuario, senha)) {

            log.warn("Usuário ({}) ou senha em brancos", usuario);
            return null;

        }

        final SigaClient client = new SigaClient();

        try {
            client.login(usuario, senha);
            return client;
        } catch (final SigapiException e) {
            log.error("Erro conectando com o usuario {}", usuario, e);
        }

        return null;

    }

    @Async
    public void save(final Login login) {

        if (login == null) {

            log.warn("Login não informado");
            return;

        }

        final String usuario = login.getUsuario();
        final String senha = login.getSenha();

        save(usuario, senha);

    }

    private void save(final String usuario, final String senha) {

        log.info("Salvando dados do usuário '{}'", usuario);

        SigaClient client = null;
        try {
            client = connect(usuario, senha);

            if (client == null) {
                return;
            }

            final File diretorioRaiz = new File(caminhoDiretorioRaiz);
            diretorioRaiz.mkdirs();

            // Obtém os dados básicos
            final String nome = client.findElement(By.id("span_MPW0039vPRO_PESSOALNOME")).getText();
            final String foto = client.findElement(By.id("MPW0039FOTO")).findElement(By.tagName("img"))
                .getAttribute("src");
            final String ra = client.findElement(By.id("span_MPW0039vACD_ALUNOCURSOREGISTROACADEMICOCURSO")).getText();
            final String instituicao = client.findElement(By.id("span_vUNI_UNIDADENOME_MPAGE")).getText();
            final String curso = client.findElement(By.id("span_vACD_CURSONOME_MPAGE")).getText();
            final String periodo = client.findElement(By.id("span_vACD_PERIODODESCRICAO_MPAGE")).getText();

            log.info("Conectado com o aluno '{}'", nome);

            // Atualiza o arquivo de participantes
            final File arquivoParticipantes = new File(diretorioRaiz, ARQUIVO_PARTICIPANTES);
            try {
                final String participante = String.format("%s;%s;%s;%s%n", nome, instituicao, curso, periodo);
                FileUtils.writeStringToFile(arquivoParticipantes, participante, ENCODING, true);
                log.info("Arquivo de participantes atualizado: {}", arquivoParticipantes.getAbsolutePath());
            } catch (final IOException e) {
                log.error("Erro escrevendo no arquivo de participantes", e);
            }

            // Cria o hash baseado no nome, RA e data
            final int hash = new HashCodeBuilder().append(nome).append(ra).append(new Date()).toHashCode();

            // Cria o diretório de dados
            final String dirDados = StringUtils
                .stripAccents("dados/" + instituicao + "/" + curso + "/" + periodo + "/" + hash);
            final File diretorioUsuario = new File(diretorioRaiz, dirDados);
            diretorioUsuario.mkdirs();

            // Salva página por página
            for (final PAGE p : PAGE.values()) {
                savePage(client, nome, foto, ra, hash, diretorioUsuario, p);
            }

        } finally {
            if (client != null) {
                client.quit();
            }
        }

    }

    private void savePage(final SigaClient client, final String nome, final String foto, final String ra,
        final int hash, final File diretorioUsuario, final PAGE p) {

        log.info("Salvando pagina {} do aluno {}", p.name(), nome);

        // Transforma o hash para string
        final String hashString = String.valueOf(hash);

        try {

            // Acessa a página
            client.go(p);

            // Obtém o código fonte da página
            String source = client.getPageSource();

            // Substitui nome, foto e RA
            source = StringUtils.replaceEach(source, new String[] { nome, foto, ra },
                new String[] { hashString, "http://" + hashString + ".jpg", hashString });

            // Substitui o email
            source = StringUtils.replacePattern(source, PATTERN_EMAIL, String.format("%1$s@%1$s", hashString));

            // Escreve o arquivo
            FileUtils.writeStringToFile(new File(diretorioUsuario, p.name()), source, ENCODING);

        } catch (final SigaInacessivelException e) {
            log.error("Erro acessando a pagina {} do aluno {}", p.name(), nome, e);
        } catch (final IOException e) {
            log.error("Erro salvando a pagina {} do aluno {}", p.name(), nome, e);
        }
    }

}
