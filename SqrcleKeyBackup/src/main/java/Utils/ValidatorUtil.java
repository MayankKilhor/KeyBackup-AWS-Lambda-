package Utils;

import Models.Circle;
import Models.SqrcleCircle;

import java.util.List;

public class ValidatorUtil {
    public static Boolean validCircleandUserID(String userId, List<Circle> circles) {
         Boolean validDetails = true;



        for (Circle circle : circles) {
            String hashAddress = HashUtil.generateHash(userId,circle.getCircleId());
            if(!circle.getCircleHash().equals(hashAddress)){
                validDetails =false;
                break;
            }
        }
        return validDetails;
    }

    public static Boolean validSqrcleCircleandUserID(String userId, List<SqrcleCircle> circles) {
        Boolean validDetails = true;



        for (SqrcleCircle circle : circles) {
            String hashAddress = HashUtil.generateHash(userId,circle.getCircleId());
            if(!circle.getCircleHash().equals(hashAddress)){
                validDetails =false;
                break;
            }
        }
        return validDetails;
    }
//
//    public static void main(String[] args){
//        Boolean validDetails = true;
//        String circleId="fb51ed1e-2291-4753-8267-bf4b162fdf74";
//        String userid="9938904622983241";
//        String check="6c83056ff6d59a5de5fa2e7a8b4861624747c6ad7da7e52e55636a5f99aa3d7d";
//        String hashAddress = HashUtil.generateHash(userid,circleId);
//        if(!check.equals(hashAddress)){
//            validDetails =false;
//
//        }
//        System.out.print(validDetails);
//    }


}
