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
    private static final String ENCODING = "UTF-8";
    private static final String PATTERN_EMAIL = "([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)";

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

            final File arquivoParticipantes = new File(diretorioRaiz, ARQUIVO_PARTICIPANTES);
            try {
                final String participante = String.format("%s;%s;%s;%s;%5$tF %5$tR %n", nome, instituicao, curso,
                    periodo, new Date());
                FileUtils.writeStringToFile(arquivoParticipantes, participante, ENCODING, true);
                log.info("Arquivo de participantes atualizado: {}", arquivoParticipantes.getAbsolutePath());
            } catch (final IOException e) {
                log.error("Erro escrevendo no arquivo de participantes", e);
            }

            final int hash = new HashCodeBuilder().append(nome).append(ra).append(new Date()).toHashCode();
            final String dirDados = StringUtils
                .stripAccents("dados/" + instituicao + "/" + curso + "/" + periodo + "/" + hash);
            final File diretorioUsuario = new File(diretorioRaiz, dirDados);
            diretorioUsuario.mkdirs();

            for (final PAGE p : PAGE.values()) {

                log.info("Salvando pagina {} do aluno {}", p.name(), nome);

                try {
                    client.go(p);
                } catch (final SigaInacessivelException e) {
                    log.error("Erro acessando a pagina {} do aluno {}", p.name(), nome, e);
                    continue;
                }

                final File arquivoTemp = new File(diretorioUsuario, p.name());
                try {

                    final String hashString = String.valueOf(hash);

                    // Obtém o código fonte da página
                    String source = (String) client.executeScript("return document.documentElement.outerHTML;");

                    // Substitui nome e RA
                    source = StringUtils.replaceEach(source, new String[] { nome, ra },
                        new String[] { hashString, hashString });

                    // Substitui o email
                    source = StringUtils.replacePattern(source, PATTERN_EMAIL, String.format("%1$s@%1$s", hashString));

                    // Escreve no arquivo
                    FileUtils.writeStringToFile(arquivoTemp, source, ENCODING);

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
