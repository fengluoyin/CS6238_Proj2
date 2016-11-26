package Server;

/**
 * Created by luoyinfeng on 11/25/16.
 */
public class MetaData {

        public byte[] encryption_key = null;
        public String security_flag = null;
        public byte[] orig_sign = null;

        public MetaData(byte[] encryption_key,String security_flag,byte[] orig_sign) {
            this.encryption_key = encryption_key;
            this.security_flag = security_flag;
            this.orig_sign = orig_sign;
        }

}
