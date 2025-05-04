package pedroaba.java.race.utils;

import net.datafaker.Faker;

import java.util.List;

public class MechanicTasksGenerator {
    public static String getTaskOnPitStop() {
        Faker faker = new Faker();

        List<String> tasks = List.of(
            "Trocar óleo do motor",
            "Inspecionar sistema de freios",
            "Alinhar e balancear rodas",
            "Substituir filtro de ar",
            "Verificar nível do fluido de transmissão",
            "Checar bateria e sistema elétrico",
            "Ajustar correias e tensores",
            "Realizar limpeza de injetores",
            "Inspecionar suspensão",
            "Testar sistema de arrefecimento",
            "Substituir pastilhas de freio",
            "Verificar sistema de escapamento",
            "Ajustar direção hidráulica",
            "Trocar velas de ignição",
            "Diagnosticar falha no motor"
        );

        return faker.options().nextElement(tasks);
    }
}
