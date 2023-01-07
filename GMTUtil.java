
import java.util.*;

public class GMTUtil {
	public static boolean filled3(String s1,String s2,String s3) {
		if (s1!=null&&s2!=null&&s3!=null) {
			if (s1.length()==0||s2.length()==0||s3.length()==0)
				return false;
			else
				return true;
		} else
			return false;
	}

	public static boolean filled1(String s1) {
		if (s1!=null) {
			if (s1.length()==0)
				return false;
			else
				return true;
		} else
			return false;
	}
}
