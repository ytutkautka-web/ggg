package fun.photon.config;

    public interface ConfigCodec {

    byte[] encode(byte[] jsonBytes);

    byte[] decode(byte[] fileBytes);
}
