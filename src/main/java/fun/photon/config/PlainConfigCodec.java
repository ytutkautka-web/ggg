package fun.photon.config;

public final class PlainConfigCodec implements ConfigCodec {

    @Override
    public byte[] encode(byte[] jsonBytes) {
        return jsonBytes;
    }

    @Override
    public byte[] decode(byte[] fileBytes) {
        return fileBytes;
    }
}
