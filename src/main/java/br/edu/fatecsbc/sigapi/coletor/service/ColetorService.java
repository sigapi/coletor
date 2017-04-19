package br.edu.fatecsbc.sigapi.coletor.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${sigapi.coletor.diretorio:/tmp/sigapi/coletor}")
    private String caminhoDiretorioRaiz;

    public boolean execute(final Login login) {
        return execute(login.getUsuario(), login.getSenha());
    }

    public boolean execute(final String usuario, final String senha) {

        final File diretorioRaiz = new File(caminhoDiretorioRaiz);
        diretorioRaiz.mkdirs();

        final SigaClient client = new SigaClient();
        try {

            log.info("Conectando com o usuario {}", usuario);

            try {
                client.login(usuario, senha);
            } catch (final SigapiException e) {
                log.error("Erro conectando com o usuario {}", usuario, e);
                return false;
            }

            final String nome = client.findElement(By.id("span_MPW0039vPRO_PESSOALNOME")).getText();
            final String ra = client.findElement(By.id("span_MPW0039vACD_ALUNOCURSOREGISTROACADEMICOCURSO")).getText();
            final String instituicao = client.findElement(By.id("span_vUNI_UNIDADENOME_MPAGE")).getText();
            final String curso = client.findElement(By.id("span_vACD_CURSONOME_MPAGE")).getText();
            final String periodo = client.findElement(By.id("span_vACD_PERIODODESCRICAO_MPAGE")).getText();

            log.info("Conectado com o aluno {} da {}", nome, instituicao);
            System.out.println(instituicao);

            final File arquivoParticipantes = new File(diretorioRaiz, ARQUIVO_PARTICIPANTES);
            try {
                FileUtils.writeStringToFile(arquivoParticipantes,
                    String.format("%s;%s;%s;%s;%5$tF %5$tR %n", nome, instituicao, curso, periodo, new Date()), true);
                log.info("Arquivo de participantes atualizado: {}", arquivoParticipantes.getAbsolutePath());
            } catch (final IOException e) {
                log.error("Erro escrevendo no arquivo de participantes", e);
            }

            final int hash = new HashCodeBuilder().append(nome).append(ra).append(new Date()).toHashCode();
            final File diretorioUsuario = new File(diretorioRaiz,
                "dados/" + instituicao + "/" + curso + "/" + periodo + "/" + hash);
            diretorioUsuario.mkdirs();

            for (final PAGE p : PAGE.values()) {

                log.info("Salvando pagina {} do aluno {}", p.name(), nome);

                try {
                    client.go(p);
                } catch (final SigaInacessivelException e) {
                    log.error("Erro acessando a pagina {} do aluno {}", p.name(), nome, e);
                    continue;
                }

                final File arquivoTemp = new File(diretorioUsuario, p.getUrl());
                try {
                    String source = client.getPageSource();
                    source = StringUtils.replaceEach(source, new String[] { nome, ra },
                        new String[] { String.valueOf(hash), String.valueOf(hash) });
                    FileUtils.writeStringToFile(arquivoTemp, source);
                } catch (final IOException e) {
                    log.error("Erro salvando a pagina {} do aluno {}", p.name(), nome, e);
                    continue;
                }

            }

        } finally {
            log.info("Desconectando");
            client.quit();
        }

        return true;

    }

}
