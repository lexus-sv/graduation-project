package main;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitInfo{

    private String title="DevPub";
    private String subtitle="Рассказы разработчиков";
    private String phone="+7 903 666-44-55";
    private String email="alakai20136@gmail.com";
    private String copyright = "Алексей Сухилин";
    private String copyrightFrom = "2019";

}
