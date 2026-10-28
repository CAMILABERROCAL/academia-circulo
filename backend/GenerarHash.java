import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarHash {
    public static void main(String[] args) {
        Console consola = System.console();

        if (consola == null) {
            throw new IllegalStateException(
                    "Ejecuta este archivo desde la terminal PowerShell.");
        }

        char[] primera = consola.readPassword("Nueva contraseña: ");
        char[] segunda = consola.readPassword("Repite la contraseña: ");

        if (primera == null || segunda == null) {
            throw new IllegalArgumentException("Ingreso cancelado.");
        }

        try {
            if (!Arrays.equals(primera, segunda)) {
                throw new IllegalArgumentException(
                        "Las contraseñas no coinciden. Vuelve a ejecutar.");
            }

            String password = new String(primera);

            if (password.length() < 12
                    || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException(
                        "Usa al menos 12 caracteres y no más de 72 bytes.");
            }

            String hash = "{bcrypt}"
                    + new BCryptPasswordEncoder().encode(password);

            System.out.println("Copia esta línea completa en pgAdmin:");
            System.out.println(hash);
        } finally {
            Arrays.fill(primera, '\0');
            Arrays.fill(segunda, '\0');
        }
    }
}