/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.crypto.ec;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.constants.ECPointFormat;
import de.rub.nds.tlsattacker.core.constants.NamedGroup;
import de.rub.nds.tlsattacker.core.exceptions.PreparationException;
import de.rub.nds.tlsattacker.core.exceptions.UnparseablePointException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PointFormatter {

    private static final Logger LOGGER = LogManager.getLogger();

    public static byte[] formatToByteArray(NamedGroup group, Point point, ECPointFormat format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int elementLength = ArrayConverter.bigIntegerToByteArray(point.getX().getModulus()).length;
        if (group != NamedGroup.ECDH_X448 && group != NamedGroup.ECDH_X25519) {
            switch (format) {
                case UNCOMPRESSED:
                    stream.write(0x04);
                    try {
                        stream.write(ArrayConverter.bigIntegerToNullPaddedByteArray(point.getX().getData(),
                                elementLength));
                        stream.write(ArrayConverter.bigIntegerToNullPaddedByteArray(point.getY().getData(),
                                elementLength));
                    } catch (IOException ex) {
                        throw new PreparationException("Could not serialize ec point", ex);
                    }
                    return stream.toByteArray();
                case ANSIX962_COMPRESSED_CHAR2:
                case ANSIX962_COMPRESSED_PRIME:
                    EllipticCurve curve = CurveFactory.getCurve(group);
                    if (curve.createAPointOnCurve(point.getX().getData()).getY().getData()
                            .equals(point.getY().getData())) {
                        stream.write(0x03);
                    } else {
                        stream.write(0x02);
                    }
                    try {
                        stream.write(ArrayConverter.bigIntegerToNullPaddedByteArray(point.getX().getData(),
                                elementLength));
                    } catch (IOException ex) {
                        throw new PreparationException("Could not serialize ec point", ex);
                    }
                    return stream.toByteArray();
                default:
                    throw new UnsupportedOperationException("Unnsupported PointFormat: " + format);

            }
        } else {
            try {
                byte[] coordinate = ArrayConverter.bigIntegerToNullPaddedByteArray(point.getX().getData(),
                        elementLength);
                stream.write(coordinate);
            } catch (IOException ex) {
                throw new PreparationException("Could not serialize ec point", ex);
            }
            return stream.toByteArray();
        }
    }

    public static byte[] toRawFormat(Point point) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int elementLenght = ArrayConverter.bigIntegerToByteArray(point.getX().getModulus()).length;
        try {
            stream.write(ArrayConverter.bigIntegerToNullPaddedByteArray(point.getX().getData(), elementLenght));
            stream.write(ArrayConverter.bigIntegerToNullPaddedByteArray(point.getY().getData(), elementLenght));
        } catch (IOException ex) {
            throw new PreparationException("Could not serialize ec point", ex);
        }
        return stream.toByteArray();
    }

    /**
     * Tries to read the first N byte[] as a point of the curve of the form x|y.
     * If the byte[] does enought bytes the base point of the named group is
     * returned
     *
     * @param group
     * @param pointBytes
     * @return
     */
    public static Point fromRawFormat(NamedGroup group, byte[] pointBytes) {
        EllipticCurve curve = CurveFactory.getCurve(group);
        int elementLenght = ArrayConverter.bigIntegerToByteArray(curve.getModulus()).length;
        if (pointBytes.length < elementLenght * 2) {
            LOGGER.warn("Cannot decode byte[] to point of " + group + ". Returning Basepoint");
            return curve.getBasePoint();
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pointBytes);
        byte[] xCord = new byte[elementLenght];
        byte[] yCord = new byte[elementLenght];
        try {
            inputStream.read(xCord);
            inputStream.read(yCord);
        } catch (IOException ex) {
            LOGGER.warn("Could not read from byteArrayStream. Returning Basepoint", ex);
            return curve.getBasePoint();
        }
        return curve.getPoint(new BigInteger(1, xCord), new BigInteger(1, yCord));

    }

    public static Point formatFromByteArray(NamedGroup group, byte[] compressedPoint) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedPoint);
        EllipticCurve curve = CurveFactory.getCurve(group);
        int elementLength = ArrayConverter.bigIntegerToByteArray(curve.getModulus()).length;
        if (compressedPoint.length == 0) {
            throw new UnparseablePointException("Could not parse point. Point is empty");
        }
        if (group != NamedGroup.ECDH_X448 && group != NamedGroup.ECDH_X25519) {
            int pointFormat = inputStream.read();
            byte[] xCoordinate = new byte[elementLength];
            switch (pointFormat) {
                case 2:
                case 3:
                    if (compressedPoint.length != elementLength + 1) {
                        throw new UnparseablePointException("Could not parse point. Point needs to be "
                                + (elementLength + 1) + " bytes long, but was " + compressedPoint.length + "bytes long");
                    }
                    try {
                        inputStream.read(xCoordinate);
                    } catch (IOException ex) {
                        LOGGER.warn("Could not read from byteArrayStream. Returning Basepoint", ex);
                        return curve.getBasePoint();
                    }
                    Point decompressedPoint = curve.createAPointOnCurve(new BigInteger(1, xCoordinate));
                    if (pointFormat == 2) {
                        decompressedPoint = curve.inverseAffine(decompressedPoint);
                    }
                    return decompressedPoint;

                case 4:
                    if (compressedPoint.length != elementLength * 2 + 1) {
                        throw new UnparseablePointException("Could not parse point. Point needs to be "
                                + (elementLength * 2 + 1) + " bytes long, but was " + compressedPoint.length
                                + "bytes long");
                    }

                    byte[] yCoordinate = new byte[elementLength];
                    try {
                        inputStream.read(xCoordinate);
                        inputStream.read(yCoordinate);
                    } catch (IOException ex) {
                        LOGGER.warn("Could not read from byteArrayStream. Returning Basepoint", ex);
                        return curve.getBasePoint();
                    }
                    return curve.getPoint(new BigInteger(1, xCoordinate), new BigInteger(1, yCoordinate));

                default:
                    throw new UnsupportedOperationException("Unnsupported PointFormat: " + pointFormat);

            }
        } else {
            if (compressedPoint.length != elementLength) {
                throw new UnparseablePointException("Could not parse point. Point needs to be " + elementLength
                        + " bytes long, but was " + compressedPoint.length + "bytes long");
            }
            byte[] xCoordinate = new byte[elementLength];
            try {
                inputStream.read(xCoordinate);
            } catch (IOException ex) {
                LOGGER.warn("Could not read from byteArrayStream. Returning Basepoint", ex);
                return curve.getBasePoint();
            }
            RFC7748Curve computation = (RFC7748Curve) curve;
            return curve.createAPointOnCurve(computation.decodeCoordinate(new BigInteger(1, xCoordinate)));
        }
    }

    private PointFormatter() {
    }
}
