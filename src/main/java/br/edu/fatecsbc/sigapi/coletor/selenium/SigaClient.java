package br.edu.fatecsbc.sigapi.coletor.selenium;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;

import br.edu.fatecsbc.sigapi.coletor.service.CredenciaisInvalidasException;
import br.edu.fatecsbc.sigapi.coletor.service.SigaInacessivelException;
import br.edu.fatecsbc.sigapi.coletor.service.SigapiException;

public class SigaClient
    extends JBrowserDriver {

    private static final int DEFAULT_TIMEOUT = 30;
    private static final int DEFAULT_IMPLICITY_TIMEOUT = 30;

    private static final String URL_LOGIN = "https://www.sigacentropaulasouza.com.br/aluno/login.aspx";

    private static final String PAGE_HOME = "home.aspx";

    private static final String ELEMENT_ID_LOGIN_INPUT_SENHA = "vSIS_USUARIOSENHA";
    private static final String ELEMENT_ID_LOGIN_INPUT_USUARIO = "vSIS_USUARIOID";
    private static final String ELEMENT_NAME_LOGIN_BT_CONFIRMA = "BTCONFIRMA";

    private static final String ELEMENT_ID_LOGIN_TXT_ERROR = "gxErrorViewer";

    private static final String ERROR_MESSAGE_LOGIN = "Não confere Login e Senha";

    public enum PAGE {

        AVISOS("Avisos", "home.aspx"),

        EMAILS(By.name("Embpage1"), AVISOS),

        CALENDARIO_PROVAS("Calendário de Provas", "calendarioprovas.aspx"),

        FALTAS_PARCIAIS("Faltas Parciais", "faltasparciais.aspx"),

        HISTORICO_GRADE("Histórico (Grade)", "historicograde.aspx"),

        HISTORICO_COMPLETO("Histórico Completo", "historicocompleto.aspx"),

        HISTORICO("Histórico", "historico.aspx"),

        HORARIO("Horário", "horario.aspx"),

        NOTAS_PARCIAIS("Notas Parciais", "notasparciais.aspx");

        private final By frame;
        private final PAGE parentPage;
        private final String label;
        private final String url;

        private PAGE(final By frame, final PAGE parentPage) {
            this(frame, parentPage, null, null);
        }

        private PAGE(final String label, final String url) {
            this(null, null, label, url);
        }

        private PAGE(final By frame, final PAGE parentPage, final String label, final String url) {
            this.frame = frame;
            this.parentPage = parentPage;
            this.label = label;
            this.url = url;
        }

        public By getFrame() {
            return frame;
        }

        public PAGE getParentPage() {
            return parentPage;
        }

        public String getLabel() {
            return label;
        }

        public String getUrl() {
            return url;
        }

        public boolean isFrame() {
            return frame != null;
        }

    }

    private static final ExpectedCondition<Boolean> ERROR_CONDITION_LOGIN = ExpectedConditions
        .textToBe(By.id(ELEMENT_ID_LOGIN_TXT_ERROR), ERROR_MESSAGE_LOGIN);

    private static final ExpectedCondition<Boolean> CONDITION_PAGE_IS_HOME = ExpectedConditions.urlContains(PAGE_HOME);

    private static final ExpectedCondition<Boolean> CONDITION_AFTER_LOGIN = ExpectedConditions
        .or(CONDITION_PAGE_IS_HOME, ERROR_CONDITION_LOGIN);

    private final int implicityTimeout;
    private PAGE currentPage;
    private boolean logged = false;

    public SigaClient() {
        this(DEFAULT_IMPLICITY_TIMEOUT);
    }

    public SigaClient(final int implicityTimeout) {
        super(createSettings());
        this.implicityTimeout = implicityTimeout;
        turnOnImplicitWaits();
    }

    private static Settings createSettings() {
        // @formatter:off
        return Settings.builder()
            .cache(false)
            .logger(null)
            .build();
        // @formatter:on
    }

    public void login(final String usuario, final String senha) throws SigapiException {

        currentPage = null;
        go(URL_LOGIN);

        if (!isElementVisibleById(ELEMENT_ID_LOGIN_INPUT_USUARIO)) {
            throw new SigaInacessivelException("Não foi possível acessar a página inicial");
        }

        sendKeysElementById(ELEMENT_ID_LOGIN_INPUT_USUARIO, usuario);
        sendKeysElementById(ELEMENT_ID_LOGIN_INPUT_SENHA, senha);
        clickElementByName(ELEMENT_NAME_LOGIN_BT_CONFIRMA);

        try {
            wait(CONDITION_AFTER_LOGIN);
        } catch (final WaitException e) {
            throw new SigaInacessivelException("Erro inesperado após o login", e);
        }

        if (ERROR_CONDITION_LOGIN.apply(this)) {
            throw new CredenciaisInvalidasException();
        }

        currentPage = PAGE.AVISOS;
        logged = true;

    }

    public void go(final PAGE newPage) throws SigaInacessivelException {

        if (!logged) {
            throw new IllegalStateException("Não está logado");
        }

        if (currentPage.isFrame()) {
            switchTo().defaultContent();
        }

        PAGE destination = newPage;
        if (newPage.isFrame()) {
            destination = newPage.getParentPage();
        }

        if (currentPage != destination) {

            // Clica no link
            findElementByLinkText(destination.getLabel()).click();
            try {
                // Aguarda o carregamento da URL
                wait(ExpectedConditions.urlContains(newPage.getUrl()));
            } catch (final WaitException e) {
                throw new SigaInacessivelException("Erro inesperado indo para a página " + newPage.name(), e);
            }

        }

        if (newPage.isFrame()) {

            try {
                wait(ExpectedConditions.frameToBeAvailableAndSwitchToIt(newPage.getFrame()));
            } catch (final WaitException e) {
                throw new SigaInacessivelException(
                    "Erro inesperado tentando obter o conteúdo de um frame da página " + newPage.name(), e);
            }

        }

        currentPage = newPage;

    }

    public void wait(final ExpectedCondition<?> condition) throws WaitException {
        wait(condition, DEFAULT_TIMEOUT);
    }

    public void wait(final ExpectedCondition<?> condition, final int timeout) throws WaitException {

        final WebDriverWait wait = new WebDriverWait(this, timeout);
        turnOffImplicitWaits();
        try {
            wait.until(condition);
        } catch (final Exception e) {
            throw new WaitException(e);
        } finally {
            turnOnImplicitWaits();
        }

    }

    public void sendKeysElementById(final String id, final String keys) {
        findElement(By.id(id)).sendKeys(keys);
    }

    public void clickElementByName(final String name) {
        findElement(By.name(name)).click();
    }

    public void go(final String url) {
        navigate().to(url);
    }

    public boolean isElementVisibleById(final String id) {
        return isElementVisible(By.id(id));
    }

    public boolean isElementVisible(final By by) {

        turnOffImplicitWaits();
        try {
            return ExpectedConditions.visibilityOfElementLocated(by).apply(this) != null;
        } catch (final NoSuchElementException e) {
            return false;
        } finally {
            turnOnImplicitWaits();
        }

    }

    @Override
    public void quit() {
        logged = false;
        super.quit();
    }

    private void turnOffImplicitWaits() {
        updateImplicityTimeout(0);
    }

    private void turnOnImplicitWaits() {
        updateImplicityTimeout(implicityTimeout);
    }

    private void updateImplicityTimeout(final int newTimeout) {
        manage().timeouts().implicitlyWait(newTimeout, TimeUnit.SECONDS);
    }

}
